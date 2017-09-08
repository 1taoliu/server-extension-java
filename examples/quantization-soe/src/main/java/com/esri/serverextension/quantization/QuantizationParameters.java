package com.esri.serverextension.quantization;

import com.esri.serverextension.core.rest.api.Extent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuantizationParameters  implements Serializable {
    private static final long serialVersionUID = 1L;


    private String mode;
    private String originPosition;
    private Double tolerance;
    private Extent extent;

    public void setMode(String mode){
        this.mode = mode;
    }
    public String getMode(){
        return this.mode;
    }

    public void setOriginPosition(String originPosition){
        this.originPosition = originPosition;
    }
    public String getOriginPosition(){
        return this.originPosition;
    }

    public void setTolerance(Double tolerance){
        this.tolerance = tolerance;
    }
    public Double getTolerance(){
        return this.tolerance;
    }

    public void setExtent(Extent extent){
        this.extent = extent;
    }
    public Extent getExtent(){
        return this.extent;
    }


}
