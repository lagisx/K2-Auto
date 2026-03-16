package com.autoatelier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Portfolio {

    private Long id;
    private String title;
    private String description;

    @JsonProperty("car_model")
    private String carModel;

    @JsonProperty("service_id")
    private Long serviceId;

    @JsonProperty("created_at")
    private String createdAt;

    private TuningService service;

    @JsonProperty("portfolio_photos")
    private List<PortfolioPhoto> photos;

    public Portfolio() {}

    public Long getId()                      { return id; }
    public String getTitle()                 { return title; }
    public String getDescription()           { return description; }
    public String getCarModel()              { return carModel; }
    public Long getServiceId()               { return serviceId; }
    public String getCreatedAt()             { return createdAt; }
    public TuningService getService()        { return service; }
    public List<PortfolioPhoto> getPhotos()  { return photos; }

    public void setId(Long id)                       { this.id = id; }
    public void setTitle(String title)               { this.title = title; }
    public void setDescription(String description)   { this.description = description; }
    public void setCarModel(String carModel)         { this.carModel = carModel; }
    public void setServiceId(Long serviceId)         { this.serviceId = serviceId; }
    public void setCreatedAt(String createdAt)       { this.createdAt = createdAt; }
    public void setService(TuningService service)    { this.service = service; }
    public void setPhotos(List<PortfolioPhoto> p)    { this.photos = p; }

    public String getCoverUrl() {
        if (photos != null && !photos.isEmpty()) {
            return photos.get(0).getPhotoUrl();
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortfolioPhoto {
        private Long id;

        @JsonProperty("portfolio_id")
        private Long portfolioId;

        @JsonProperty("photo_url")
        private String photoUrl;

        @JsonProperty("created_at")
        private String createdAt;

        public PortfolioPhoto() {}

        public Long getId()            { return id; }
        public Long getPortfolioId()   { return portfolioId; }
        public String getPhotoUrl()    { return photoUrl; }
        public String getCreatedAt()   { return createdAt; }

        public void setId(Long id)                  { this.id = id; }
        public void setPortfolioId(Long portfolioId){ this.portfolioId = portfolioId; }
        public void setPhotoUrl(String photoUrl)    { this.photoUrl = photoUrl; }
        public void setCreatedAt(String createdAt)  { this.createdAt = createdAt; }
    }
}
