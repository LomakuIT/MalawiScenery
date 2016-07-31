/*
 * Copyright 2016 Lomaku Technologies Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inc.automata.malawiscenery.model;

public class Category {
	public String id, title, photoNo;
	 
    public Category() {    
    }
 
    public Category(String id, String title,String photoNo) {
        this.id = id;
        this.title = title;
        this.photoNo =photoNo;
    }
 
    public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }
 
    public String getTitle() {
        return title;
    }
 
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoNo() {
        return photoNo;
    }

    public void setPhotoNo(String photoNo) {
        this.photoNo = photoNo;
    }
}
