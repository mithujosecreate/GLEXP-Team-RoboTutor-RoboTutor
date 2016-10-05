//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.ltkplus;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

import cmu.xprize.util.TCONST;


// TODO: Convert to singleton
//
public class CRecognizerPlus implements IGlyphSink {

    private static CRecognizerPlus ourInstance = new CRecognizerPlus();

    private Context                mContext;

    private CGlyphSet              _glyphSet;
    private ConcurrentLinkedQueue  _glyphQueue = new ConcurrentLinkedQueue();
    private boolean                mDisabled   = false;

    private RecognizerTask         _recTask;
    private QueuedGlyph            _nextGlyph;

    private Boolean                _isRecognizing = false;
    private CStroke[]              _recStrokes;

    private CRecResult[]           _ltkCandidates     = null;
    private CRecResult[]           _ltkPlusCandidates = null;
    private int                    _sampleIndex       = 0;

    private Rect                   _viewBnds          = new Rect();  // The view draw bounds
    private Rect                   _fontBnds          = new Rect();  // The bounds for the font size limits
    private float                  _baseLine;
    private float                  _dotSize;
    private Paint                  _Paint;

    private String                 _sampleExpected    = null;
    private CGlyph                 _drawGlyph         = null;

    private boolean                _boostExpected     = true;
    private boolean                _boostUnExpected   = true;
    private boolean                _boostPunctuation  = false;
    private boolean                _boostMissingSample= false;

    private boolean                _boostDigitClass   = false;
    private boolean                _boostAlphaClass   = false;
    private boolean                _boostSampleClass  = true;


    private CLipiTKJNIInterface _recognizer;

    private static final String   TAG = "CRecognizer";


    private CRecognizerPlus() {
    }

    public void initialize(Context context, String alphabet) {

        mContext = context;

        // Initialize lipitk

        String path = context.getExternalFilesDir(null).getPath();

        Log.d("JNI", "Path: " + path);

        try {
            _recognizer = new CLipiTKJNIInterface(path, "SHAPEREC_ALPHANUM");
            //   _recognizer = new LipiTKJNIInterface(path, "SHAPEREC_NUMERALS");

            _recognizer.initialize();
        }
        catch(Exception e)
        {
            Log.d(TAG, "Cannot create Recognizer - Error:1");
            System.exit(1);
        }

        // Load the prototype glyphs
        //
        _glyphSet = new CGlyphSet(mContext, alphabet);
    }


    /**
     * Singleton Instance
     *
     * @return
     */
    public static CRecognizerPlus getInstance() {
        return ourInstance;
    }


    public CGlyphSet getGlyphPrototypes() {
        return _glyphSet;
    }


    /**
     * We manage commands coming from the enclosing component here.
     *
     * @param command
     */
    @Override
    public void execCommand(String command) {

        switch(command) {

        }
    }

    public boolean toggleExpectedBoost() {

        _boostExpected = !_boostExpected;

        return _boostExpected;
    }

    public boolean toggleUnExpectedBoost() {

        _boostUnExpected = !_boostUnExpected;

        return _boostUnExpected;
    }

    public boolean togglePunctBoost() {

        _boostPunctuation = !_boostPunctuation;

        return _boostPunctuation;
    }

    public void setClassBoost(String classID) {

        _boostAlphaClass = false;
        _boostDigitClass = false;
        _boostSampleClass = false;

        switch(classID) {
            case GCONST.BOOST_ALPHA:
                _boostAlphaClass = true;
                break;
            case GCONST.BOOST_DIGIT:
                _boostDigitClass = true;
                break;
            case GCONST.BOOST_EXCLASS:
                _boostSampleClass = true;
                break;
        }
    }



    /**
     * Remove any pending scenegraph commands.
     *
     */
    public void flushQueue() {

        _glyphQueue = new ConcurrentLinkedQueue();
    }


    /**
     *
     */
    public class QueuedGlyph {

        protected IGlyphSource _source;
        protected CGlyph _glyph;

        public QueuedGlyph(IGlyphSource source, CGlyph glyph) {

            _source  = source;
            _glyph   = glyph;
        }
    }


    /**
     * Add a glyph to the recognition queue
     *
     */
    public void postToQueue(IGlyphSource source, CGlyph glyph) {

        _glyphQueue.add(new QueuedGlyph(source, glyph));

        // Try and recognize it if not already working on another one.
        //
        nextQueued();
    }


    private void nextQueued() {

        synchronized (_isRecognizing) {

            if(!_isRecognizing) {

                _nextGlyph = (QueuedGlyph) _glyphQueue.poll();

                if (_nextGlyph != null) {

                    _isRecognizing = true;

                    // Tasks can only run once so create a new one for each recognition task.
                    _recTask = new RecognizerTask();
                    _recTask.execute();
                }
            }
        }
    }


    /**
     * The RecognizerThread provides a background thread on which to do the rocognition task
     * TODO: We may need a scrim on the UI thread depending on the observed performance
     */
    class RecognizerTask extends AsyncTask<Void, Void, String> {

        private long         LTKTimer;                  // Used for benchmarking
        private long         LTKPlusTimer;              // Used for benchmarking

        RecognizerTask() {
        }


        public void setStrokes(CStroke[] _recognitionStrokes) {
            _recStrokes = _recognitionStrokes;
        }


        /** This is processed on the background thread - when it returns OnPostExecute is called or
         // onCancel if it was cancelled -
         */
        @Override
        protected String doInBackground(Void... unsued) {

            _ltkCandidates = _recognizer.recognize(_recStrokes);

            LTKPlusTimer = System.currentTimeMillis();
            Log.d("LTKPLUS", "Time in LTKProcessor: " + (System.currentTimeMillis() - LTKTimer));

            // generate the LTK project folder that contains the symbol to unicode mapping
            //
            String configFileDirectory = _recognizer.getLipiDirectory() + "/projects/alphanumeric/config/";
//            String configFileDirectory = _recognizer.getLipiDirectory() + "/projects/demonumerals/config/";

            for (int i = 0; i < _ltkCandidates.length; i++) {

                _ltkCandidates[i].setRecChar(_recognizer.getSymbolName(_ltkCandidates[i].Id, configFileDirectory));

                Log.d("LTK", "Char = " + _ltkCandidates[i].getRecChar() + " Confidence = " + _ltkCandidates[i].Confidence + "  - ShapeID = " + _ltkCandidates[i].Id);
            }

            _sampleIndex = ltkPlusProcessor(_nextGlyph._source );

            return null;
        }


        /** OnPostExecute is guaranteed to run on the UI thread so we can update the view etc
         // TODO: update this to do something useful
         */
        @Override
        protected void onPostExecute(String sResponse) {

            _recStrokes = null;

            Log.d("LTKPLUS", "Time in LTK_PLUS_Processor: " + (System.currentTimeMillis() - LTKPlusTimer));

            synchronized (_isRecognizing) {

                _isRecognizing = false;

                _nextGlyph._source.recCallBack(_ltkCandidates, _ltkPlusCandidates, _sampleIndex);

                // Check if any more recognizer requests are queued
                //
                nextQueued();
            }
        }


        /**
         *  The recognizer expects an "array" of strokes so we generate that here in the UI thread
         *  from the ArrayList of captured strokes.
         *
         */
        @Override
        protected void onPreExecute() {

            LTKTimer = System.currentTimeMillis();

            CGlyph glyph = _nextGlyph._glyph;

            _recStrokes = new CStroke[glyph.size()];

            for (int s = 0; s < glyph.size(); s++)
                _recStrokes[s] = glyph.getStroke(s);
        }
    }




    //**********************************************************************************
    //**********************************************************************************
    //**********************************************************************************
    // LTK+ processor


    private int ensureCandidate(String newCandidate, boolean force) {

        CRecResult[] insCandidates;
        boolean      hasCandidate = false;
        int          insertIndex  = -1;
        int          lowestIndex  = 0;

        if(!force) {
            for (int i1 = 0; i1 < _ltkCandidates.length; i1++) {

                if (newCandidate.equals(_ltkCandidates[i1].getRecChar())) {
                    hasCandidate = true;
                    break;
                }
            }
        }

        if(!hasCandidate || force) {
            insertIndex = _ltkCandidates.length;

            insCandidates = new CRecResult[_ltkCandidates.length + 1];

            for(int i1 = 0; i1 < _ltkCandidates.length ; i1++) {

                insCandidates[i1] = _ltkCandidates[i1];

                // Keep track of the lowest LTK confidence
                //
                if(!_ltkCandidates[i1].isVirtual()) {
                    lowestIndex = i1;
                }
            }

            // Give it the same confidence as the second most likely
            // Handle special case where there is one or no candidates
            // TODO: The value of 0.5f here is totally arbitrary - i.e. may not be rational
            //
            if(_ltkCandidates.length <= 1) {
                insCandidates[insertIndex] = new CRecResult(newCandidate, 0.5f, true);
            }
            else {

                insCandidates[insertIndex] = new CRecResult(newCandidate, _ltkCandidates[lowestIndex].Confidence, true);
            }
            insCandidates[insertIndex].updateORConfidence(insCandidates[insertIndex].Confidence);
            insCandidates[insertIndex].setVisualRequest(true);

            _ltkCandidates = insCandidates;
        }

        return insertIndex;
    }


    private int addVirutalCandidate(String newCandidate) {

        return ensureCandidate(newCandidate, GCONST.FORCE);
    }


    private boolean ensureAlternateCase(String cand, int candIndex) {

        boolean forceProcessing = false;
        char    candidate       = cand.charAt(0);

        if(Character.isAlphabetic(candidate)) {

            boolean isUpper  = Character.isUpperCase(candidate);
            char    testChar = Character.toLowerCase(candidate);

            // Scan for the alternate case version of the candidate char if it exists give it the
            // same confidence
            //
            for(int i1 = 0 ; i1 < _ltkCandidates.length ; i1++) {

                // Don't match the candidate itself
                //
                if((i1 != candIndex) && testChar == Character.toLowerCase(_ltkCandidates[i1].getRecChar().charAt(0))) {

                    // Make the upper and lower the same confidence and let them battle it out
                    //
                    _ltkCandidates[i1].setPlusConfidence(_ltkCandidates[candIndex].getPlusConfidence());

                    forceProcessing = true;
                    Log.d(TAG, "Force LTK+ processing");
                    break;
                }
            }

            // If we don't find the alternate case of the character then add it.
            // This allows free form - extemporaneous input - where there are no expectations
            // set on the input character.
            //
            if(!forceProcessing) {

                int newIndex = addVirutalCandidate(isUpper? cand.toLowerCase(): cand.toUpperCase());

                _ltkCandidates[newIndex].setPlusConfidence(_ltkCandidates[candIndex].Confidence);

                forceProcessing = true;
                Log.d(TAG, "Force LTK+ virtual processing");
            }
        }

        return forceProcessing;
    }


    /**
     *
     */
    public int ltkPlusProcessor(IGlyphSource glyphSrc ) {

        String          candidateLTK;
        char            sampleChar;
        CGlyphMetrics   metric;
        char            ltkChar;
        Rect            compCharBnds    = new Rect();
        RectF           glyphVisualBnds;
        int             sampleIndex     = GCONST.EXPECTED_NOT_FOUND;
        boolean         forceProcessing = false;
        boolean         sampDigit       = false;
        boolean         sampUpper       = false;
        boolean         sampAlpha       = false;

        _sampleExpected    = glyphSrc.getExpectedChar();
        _drawGlyph         = glyphSrc.getGlyph();
        _fontBnds          = glyphSrc.getFontBnds();
        _viewBnds          = glyphSrc.getViewBnds();
        _baseLine          = glyphSrc.getBaseLine();
        _dotSize           = glyphSrc.getDotSize();
        _Paint             = glyphSrc.getPaint();

        // Filter invalid character expecations.
        //
        if(_sampleExpected == null || _sampleExpected == "") {
            _sampleExpected = " ";
            sampleIndex     = GCONST.EXPECT_NONE;
        }

        candidateLTK = _ltkCandidates[0].getRecChar();
        ltkChar      = candidateLTK.charAt(0);

        // The LTK inferred confidence is the Confidence property
        // Initialize the "overall Confidence"  LTK+ inferred confidence
        // locate the position of the sampleChar in the recognition set. (it may not be there)
        //
        _ltkCandidates[0].setIsBestLTK(true);

        for(int i1 = 0; i1 < _ltkCandidates.length ; i1++) {

            _ltkCandidates[i1].updateORConfidence(_ltkCandidates[i1].Confidence);

            // We make an assumption that LTK will never produce " " as a candidate
            //
            if(_ltkCandidates[i1].getRecChar().equals(_sampleExpected)) {
                _ltkCandidates[i1].setIsExpected(true);
                sampleIndex = i1;
            }
        }

        // If the sample (i.e. expected character) is not amongst the LipiTK candidates then we add it.
        //
        // We are correcting for specific LipiTK named recognizer errors (i.e. the ALPHANUM recognizer) -
        // Sometime it doesn't produce certain characters that you'd think it should due to training
        // deficiencies - we force add boostMap characters here -
        //
        if(sampleIndex == GCONST.EXPECTED_NOT_FOUND && (_boostMissingSample || GCONST.boostMap.containsKey(_sampleExpected))) {

            // Special processing = LTK essentially never recognizes a comma nor an apostrophe.
            // so to allow the system to recognize either and disambiguate the result we add
            // both -
            //
            switch(_sampleExpected) {
                case ",":
                    addVirutalCandidate("\'");
                    break;

                case "'":
                    addVirutalCandidate(",");
                    break;

            }
            sampleIndex = addVirutalCandidate(_sampleExpected);
        }

        // If boosting punctuation for extemporaneous input add relevant punctuation
        // so they are always considered.
        //
        if(_boostPunctuation) {
            ensureCandidate(".",  GCONST.NOFORCE);
            ensureCandidate(",",  GCONST.NOFORCE);
            ensureCandidate(";",  GCONST.NOFORCE);
            ensureCandidate("\'", GCONST.NOFORCE);
        }

        // Special processing -
        // For whatever "alphabetic" LipiTK thinks is correct...
        // Ensure both case alternates are candidates then force post-processing to disambiguate case.
        // This disambiguates X from x or O from o etc.
        // By always doing it we allow for partial extemporaneous input - i.e. they don't input the expected character
        // Note that this does nothing if it is not alphabetic
        // Note if we are boosting the expected then we don't add the alternate of LTKBest if it is
        //      not the expected sample.
        //
        if((sampleIndex > 0) && !_boostExpected)
            forceProcessing |= ensureAlternateCase(candidateLTK, 0);


        // We always ensure the alternate case of the expected is present as we know that LTK is often wrong on case
        // differentiation and we want both case alternates of the expected char to be evaluated e.g. X and x
        //
        if(sampleIndex >= 0)
            forceProcessing |= ensureAlternateCase(_sampleExpected, sampleIndex);

        // Generate the visual bounds for the glyph that was drawn
        //
        glyphVisualBnds = _drawGlyph.getGlyphViewBounds(_viewBnds, GCONST.STROKE_WEIGHT);


        // If the LTK candidate is not the sample char then do LTK+ post processing.
        // If we added alternate cases or virtual cases then forceprocessing
        // If we want to boost extemporaneous input (boostUnExpected) then force processing i.e. boost
        // other than the lipiTK output based on topological metrics from LTK+
        //
        if(sampleIndex > 0 || forceProcessing || _boostUnExpected) {

            _ltkPlusCandidates = new CRecResult[_ltkCandidates.length];
            int index = 0;


            if(sampleIndex != GCONST.EXPECT_NONE) {

                compCharBnds = glyphSrc.getFontCharBounds(_sampleExpected);

                sampleChar = _sampleExpected.charAt(0);

                sampDigit = Character.isDigit(sampleChar);
                sampUpper = Character.isUpperCase(sampleChar);
                sampAlpha = Character.isLetterOrDigit(sampleChar);
            }

            // Clone a prototype glyph for each LTK candidate
            //
            for(CRecResult candidate : _ltkCandidates) {

                candidate.setGlyph(new CGlyph(mContext, _baseLine, _viewBnds, _dotSize));

                metric = candidate.getGlyph().getMetric();

                boolean isSample     = candidate.getIsExpected();
                String  candChar     = candidate.getRecChar();
                Rect    candCharBnds = glyphSrc.getFontCharBounds( candChar);

                boolean candDigit = Character.isDigit(candChar.charAt(0));
                boolean candUpper = Character.isUpperCase(candChar.charAt(0));
                boolean candAlpha = Character.isLetterOrDigit(candChar.charAt(0));


                // Generate the dimensional metrics for the given candidate char
                // Generate the visual comparison metric for the given char
                //
                metric.calcMetrics(_drawGlyph, glyphVisualBnds, candCharBnds, (sampleIndex != GCONST.EXPECT_NONE)? compCharBnds:candCharBnds , GCONST.STROKE_WEIGHT);

                candidate.setVisualConfidence(metric.generateVisualMetric(_fontBnds, candChar, (sampleIndex != GCONST.EXPECT_NONE)? _sampleExpected:candChar, _drawGlyph, _Paint, GCONST.CALIBRATED_WEIGHT, TCONST.VOLATILE));

                Log.d("Metrics Initial: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence())) ;

                //*************************************************************************
                // NOTE: in all of these the (isSample)? 0.5f:0.5f) the absolute values are not important but only
                //       the spread. i.e. delta

                // Disambiguate puntucation from non-puntuation
                //
                if((sampleIndex != GCONST.EXPECT_NONE) && (candAlpha != sampAlpha)) {

                    candidate.updateORConfidence((isSample)? 0.7f:0.5f);
                    Log.d("Metrics punct MM: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence())) ;
                }


                // Manage class boosts
                //
                if(_boostDigitClass) {
                    candidate.updateORConfidence((candDigit)? 1.0f:0.0f);

                    Log.d("Metrics Digit Boost: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence())) ;
                }

                else if(_boostAlphaClass) {
                    candidate.updateORConfidence((candAlpha)? 1.0f:0.0f);

                    Log.d("Metrics Alpha Boost: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence())) ;
                }

                else if(_boostSampleClass && (candDigit != sampDigit)) {
                    candidate.updateORConfidence((isSample)? 0.75f:0.3f);

                    Log.d("Metrics Class Boost: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence())) ;
                }


                candidate.updateANDConfidence(1.0f - metric.getDeltaA(), (isSample)? 0.5f:0.4f);
                Log.d("Metrics Aspect: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence()) + "  Aspect: " + String.format("%.3f", metric.getDeltaA()));

                candidate.updateANDConfidence(1.0f - metric.getDeltaH(), (isSample)? 0.6f:0.5f);
                Log.d("Metrics Height: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence()) + "  DeltaH: " + String.format("%.3f", metric.getDeltaH()));

                candidate.updateANDConfidence(1.0f - metric.getDeltaY(), (isSample)? 0.6f:0.5f);
                Log.d("Metrics Vertical: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence()) + "  DeltaY: " + String.format("%.3f", metric.getDeltaY()));

                // Update the visual confidence
                //
                candidate.updateANDConfidence(candidate.getVisualConfidence(), (isSample)? 0.75f:0.5f);
                Log.d("Metrics Visual: ", candChar + ":" + String.format("%.3f", candidate.getPlusConfidence()) + "  Visual: " + String.format("%.3f", candidate.getVisualConfidence()));

                _ltkPlusCandidates[index++] = candidate;
            }

            // Sort the LTK+ array by Plus confidence levels
            //
            int        count      = _ltkPlusCandidates.length;
            boolean    sortResult = true;
            CRecResult temp;

            while(sortResult) {

                sortResult = false;
                for(int i1 = 1 ; i1 < count ; i1++) {

                    if (_ltkPlusCandidates[i1-1].getPlusConfidence() < _ltkPlusCandidates[i1].getPlusConfidence()) {
                        temp = _ltkPlusCandidates[i1-1];

                        _ltkPlusCandidates[i1-1] = _ltkPlusCandidates[i1];
                        _ltkPlusCandidates[i1]   = temp;
                        sortResult = true;
                    }
                }
                count--;
            }
        }

        // If the top LTK candidate matches the expected char then just mirror the results
        //
        else {
            CRecResult candidate;

            _ltkPlusCandidates = _ltkCandidates;

            // calc metrics on the ltk candidate to provide the tutor with character quality data
            //
            candidate = _ltkCandidates[0];
            candidate.setGlyph(_drawGlyph);

            metric = candidate.getGlyph().getMetric();

            String  candChar     = candidate.getRecChar();
            Rect    candCharBnds = glyphSrc.getFontCharBounds( candChar);

            // Generate the dimensional metrics for the given candidate char
            // Generate the visual comparison metric for the given char
            //
            metric.calcMetrics(_drawGlyph, glyphVisualBnds, candCharBnds, (sampleIndex != GCONST.EXPECT_NONE)? compCharBnds:candCharBnds , GCONST.STROKE_WEIGHT);

            candidate.setVisualConfidence(metric.generateVisualMetric(_fontBnds, candChar, (sampleIndex != GCONST.EXPECT_NONE)? _sampleExpected:candChar, _drawGlyph, _Paint, GCONST.CALIBRATED_WEIGHT, TCONST.VOLATILE));
        }

        // If there is no valid sample in the recognition set then use the TLK candidate = i.e. elemenet 0
        //
        if(sampleIndex < 0)
            sampleIndex = 0;

        return sampleIndex;
    }
}


