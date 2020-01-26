package com.mimik.smarthome.edgeSDK;

import java.util.Date;

public class DriveFile {

    public static class ImageMediaMetadata {
        public int width;
        public int height;
    }

    public static class VideoMediaMetadata {
        public int width;
        public int height;
        public long durationMillis;
    }

    public final String kind = "drive#file";
    public String id;
    public String name;
    public String mimeType;
    public String description;
    //public List<String> parents;
    public String thumbnailLink;
    public Date createTime;
    public String fullFileExtension;
    public Long size;
    public ContentHints contentHints;

    public transient String contentLink;

    public ImageMediaMetadata imageMediaMetadata;
    public VideoMediaMetadata videoMediaMetadata;
}
