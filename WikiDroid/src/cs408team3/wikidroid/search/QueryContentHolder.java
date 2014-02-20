/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cs408team3.wikidroid.search;

/**
 * 
 * @author Felipe
 */
public class QueryContentHolder {

    String title;
    String link;
    String displayLink;

    public QueryContentHolder(String title, String link, String displayLink) {
        this.title = title;
        this.link = link;
        this.displayLink = displayLink;
    }

    @Override
    public String toString() {
        return "QueryContentHolder{" + "title=" + title + ", link=" + link + ", displayLink=" + displayLink + '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDisplayLink() {
        return displayLink;
    }

    public void setDisplayLink(String displayLink) {
        this.displayLink = displayLink;
    }

}
