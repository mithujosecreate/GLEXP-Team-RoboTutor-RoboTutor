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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.ILoadableObject;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;

/**
 * Scene Descriptors are used for simple CTutorNavigator instances
 * which use a linear list of scenes
 */
public class scene_descriptor implements ILoadableObject {

    private TScope                       _scope;

    // json loadable fields
    public int                           index;
    public ITutorSceneImpl               instance;
    public HashMap<String, ITutorObject> children;

    public String       id;
    public String       classname;

    public String       title;
    public String       page;
    public String       comment;

    public Boolean      enqueue;
    public Boolean      create;
    public Boolean      persist;
    public Boolean      ischeckpnt;

    public String       features;


    public scene_descriptor() {
    }


    // *** Serialization


    public void loadJSON(JSONObject jsonObj, TScope scope) {

        _scope = scope;

        JSON_Helper.parseSelf(jsonObj, this, scope);
    }
}
