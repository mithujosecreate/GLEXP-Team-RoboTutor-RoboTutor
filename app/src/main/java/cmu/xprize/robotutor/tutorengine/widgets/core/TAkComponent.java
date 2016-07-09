package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.ak_component.CAk_Component;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;

/**
 * Created by jacky on 2016/7/6.
 */

public class TAkComponent extends CAk_Component{

    private CTutor mTutor;
    private CObjectDelegate mSceneObject;


    static final String TAG = "TAkComponent";

    public TAkComponent(Context context) {
        super(context);
    }

    public TAkComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TAkComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface
//
//    @Override
//    public void UpdateValue(int value) {
//
//        // update the Scope response variable  "<varname>.value"
//        //
//        mTutor.getScope().addUpdateVar(name() + ".value", new TInteger(value));
//
//        boolean correct = isCorrect();
//
//        reset();
//
//        if(correct)
//            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
//        else
//            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
//    }
//
//
//    private void reset() {
//
//        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
//        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
//    }
//
//
//    /**
//     * Preprocess the data set
//     *
//     * @param data
//     */
//    @Override
//    protected void updateDataSet(CBp_Data data) {
//
//        // Let the compoenent process the new data set
//        //
//        super.updateDataSet(data);
//    }
//
//
//
//    /**
//     *
//     * @param dataSource
//     */
//    public void setDataSource(String dataSource) {
//
//        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
//        // on the first pass.
//        //
//        reset();
//
//        // We make the assumption that all are correct until proven wrong
//        //
//        mTutor.setAddFeature(TCONST.ALL_CORRECT);
//
//        // TODO: globally make startWith type TCONST
//        try {
//            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
//                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());
//
//                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataSource);
//                // Load the datasource in the component module - i.e. the superclass
//                loadJSON(new JSONObject(jsonData), null);
//
//            } else if (dataSource.startsWith("db|")) {
//
//
//            } else if (dataSource.startsWith("{")) {
//
//                loadJSON(new JSONObject(dataSource), null);
//
//            } else {
//                throw (new Exception("BadDataSource"));
//            }
//        }
//        catch (Exception e) {
//            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), e, false);
//        }
//    }
//
//
//    public void next() {
//
//        // If wrong reset ALLCORRECT
//        //
//        if(mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {
//
//            mTutor.setDelFeature(TCONST.ALL_CORRECT);
//        }
//
//        reset();
//
//        super.next();
//
//        if(dataExhausted())
//            mTutor.setAddFeature(TCONST.FTR_EOI);
//    }
//
//
//    public void enable(Boolean enable) {
//    }
//
//
//    public void setButtonBehavior(String command) {
//        mSceneObject.setButtonBehavior(command);
//    }
//
//
//
//    //**********************************************************
//    //**********************************************************
//    //*****************  Common Tutor Object Methods
//
//    @Override
//    public void onDestroy() {
//
//    }
//
//    @Override
//    public void setName(String name) {
//        mSceneObject.setName(name);
//    }
//
//    @Override
//    public String name() {
//        return mSceneObject.name();
//    }
//
//    @Override
//    public void setParent(ITutorSceneImpl mParent) {
//        mSceneObject.setParent(mParent);
//    }
//
//    @Override
//    public void setTutor(CTutor tutor) {
//        mTutor = tutor;
//        mSceneObject.setTutor(tutor);
//    }
//
//    @Override
//    public void postInflate() {}
//
//    @Override
//    public void setNavigator(ITutorGraph navigator) {
//        mSceneObject.setNavigator(navigator);
//    }
//
//    @Override
//    public void setLogManager(ILogManager logManager) {
//        mSceneObject.setLogManager(logManager);
//    }
//
//
//    @Override
//    public CObjectDelegate getimpl() {
//        return mSceneObject;
//    }
//
//    @Override
//    public void zoomInOut(Float scale, Long duration) {
//
//    }
//
//    @Override
//    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition) {
//
//    }
//
//    @Override
//    public void setAlpha(Float alpha) {
//
//    }

}
