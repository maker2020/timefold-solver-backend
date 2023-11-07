package cn.keyvalues.optaplanner.common.geo;

/**
 * 坐标点类型
 */
public enum PointType {

    /**
     * 墨卡托，为一种大地坐标系，也是目前广泛使用的GPS全球卫星定位系统使用的坐标系。
     */
    WGS84,

    /**
     * 又称火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
     */
    GCJ02,

    /**
     * 百度坐标系，在GCJ02坐标系基础上再次加密。其中bd09ll表示百度经纬度坐标，bd09mc表示百度墨卡托米制坐标。
     * 非中国地区地图，服务坐标统一使用WGS84坐标
     */
    BD09,

    /**
     * 未知
     */
    UNKNOWN
}
