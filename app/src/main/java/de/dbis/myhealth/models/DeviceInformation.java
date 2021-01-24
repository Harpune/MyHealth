package de.dbis.myhealth.models;

public class DeviceInformation {
    // hardware
    private String device;
    private String hardware;
    private String manufacturer;
    private String model;
    private String product;

    // software
    private int sdk;
    private String os;

    public DeviceInformation(String device, String hardware, String manufacturer, String model, String product, int sdk, String os) {
        this.device = device;
        this.hardware = hardware;
        this.manufacturer = manufacturer;
        this.model = model;
        this.product = product;
        this.sdk = sdk;
        this.os = os;
    }
}
