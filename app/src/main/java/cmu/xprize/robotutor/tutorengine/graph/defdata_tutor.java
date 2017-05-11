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

package cmu.xprize.robotutor.tutorengine.graph;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Scene Descriptors are used for simple CTutorGraph instances
 * which use a linear list of scenes
 */
public class defdata_tutor implements ILoadableObject2 {

    private final String TAG = "defdata_tutor";

    // json loadable fields
    // Map scene names to component data bindings

    public HashMap<String, defdata_scenes> scene_bindings;

    public defdata_tutor() {
        scene_bindings = new HashMap<>();
    }


    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        try {
            JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);
        }
        catch(Exception e) {
            Log.i(TAG, "Scene Load Failed: " + e);
        }
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
