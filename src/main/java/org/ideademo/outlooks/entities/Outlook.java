package org.ideademo.outlooks.entities;

import java.lang.Comparable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import org.apache.tapestry5.beaneditor.NonVisual;

@Entity @Indexed
public class Outlook implements Comparable<Outlook>
{
	
	//////////////////////////////////////////
	//Reserved indexing id 
	
	@Id @GeneratedValue @DocumentId @NonVisual
	private Long id;
	
	
	//////////////////////////////////////////////
	//String fields (being a keyword for Lucene)
	//
	
	@Field
	private String code;

	@Field @Column (length=1024)
	private String name;
	
	@Field  @Column (length=2048)
	private String organization;
	
	@Field  @Column (length=2048)
	private String contact;
	
	@Field 
	private String url;
	
	@Field @Column (length=4096)
	private String description;
	
	@Field @Column (length=4096)
	private String keywords;
	
	@Field 
	private String worksheet;
	
	 //Region/Locale
	private boolean centralNorthPacific = false;
	    private boolean stateOfHawaii = false;
	    private boolean northWestHawaiianIslands = false;
	    private boolean pacificRemoteIslands = false;

	private boolean westernNorthPacific = false;
	    private boolean cnmi = false;
	    private boolean fsm = false;
	    private boolean guam = false;
	    private boolean palau = false;
	    private boolean rmi = false;
	    private boolean otherWesternNorthPacific = false;
	    
	private boolean southPacific = false;
	    private boolean americanSamoa = false;
	    private boolean australia = false;
	    private boolean cookIslands = false; 
	    private boolean fiji = false;
	    private boolean frenchPolynesia = false;
	    private boolean kiribati = false; 
	    private boolean newZealand = false;
	    private boolean png = false; 
	    private boolean samoa = false;
	    private boolean solomonIslands = false; 
	    private boolean tonga = false;
	    private boolean tuvalu = false; 
	    private boolean vanuatu = false; 
	    private boolean otherSouthPacific = false;
	    
	private boolean pacificBasin = false;
	private boolean global = false;

	// ECV
	private boolean atmosphericData = false;
	private boolean oceanicData = false;
	private boolean terrestrialData = false;
	
	//Phenomenal Impacts
	private boolean drought = false; 
	private boolean rainfall = false;
	private boolean flooding = false; 
	private boolean bleaching = false; 
	private boolean otherPhenomena = false;
	

	//Spatial Scale 
	private boolean grid = false;
	private boolean point = false;
	
	//Time Scale
	private boolean past = false;
	private boolean current = false;
	private boolean future = false;
 	    private boolean oneMonth = false;
	    private boolean threeMonths = false;
	    private boolean sixMonths = false;
	
	//Methodology
	private boolean insitu = false;
	private boolean remote = false;
	private boolean statistical = false;
	private boolean dynamical = false;
	
	// Sector
	private boolean health = false; 
	private boolean freshWater = false;
	private boolean energy = false;
	private boolean transportation = false;
	private boolean planning = false;
	private boolean socioCultural = false;
	private boolean agriculture = false;
	private boolean recreation = false;
	private boolean ecological = false;
	private boolean otherSector= false;
	
	  
	//////////////////////////////////////////////
	//  Internal 
	//
	private boolean worksheetExists = false;


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getOrganization() {
		return organization;
	}


	public void setOrganization(String organization) {
		this.organization = organization;
	}


	public String getContact() {
		return contact;
	}


	public void setContact(String contact) {
		this.contact = contact;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getKeywords() {
		return keywords;
	}


	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}


	public String getWorksheet() {
		return worksheet;
	}


	public void setWorksheet(String worksheet) {
		this.worksheet = worksheet;
	}


	public boolean isCentralNorthPacific() {
		return centralNorthPacific;
	}


	public void setCentralNorthPacific(boolean centralNorthPacific) {
		this.centralNorthPacific = centralNorthPacific;
	}


	public boolean isStateOfHawaii() {
		return stateOfHawaii;
	}


	public void setStateOfHawaii(boolean stateOfHawaii) {
		this.stateOfHawaii = stateOfHawaii;
	}


	public boolean isNorthWestHawaiianIslands() {
		return northWestHawaiianIslands;
	}


	public void setNorthWestHawaiianIslands(boolean northWestHawaiianIslands) {
		this.northWestHawaiianIslands = northWestHawaiianIslands;
	}


	public boolean isPacificRemoteIslands() {
		return pacificRemoteIslands;
	}


	public void setPacificRemoteIslands(boolean pacificRemoteIslands) {
		this.pacificRemoteIslands = pacificRemoteIslands;
	}


	public boolean isWesternNorthPacific() {
		return westernNorthPacific;
	}


	public void setWesternNorthPacific(boolean westernNorthPacific) {
		this.westernNorthPacific = westernNorthPacific;
	}


	public boolean isCnmi() {
		return cnmi;
	}


	public void setCnmi(boolean cnmi) {
		this.cnmi = cnmi;
	}


	public boolean isFsm() {
		return fsm;
	}


	public void setFsm(boolean fsm) {
		this.fsm = fsm;
	}


	public boolean isGuam() {
		return guam;
	}


	public void setGuam(boolean guam) {
		this.guam = guam;
	}


	public boolean isPalau() {
		return palau;
	}


	public void setPalau(boolean palau) {
		this.palau = palau;
	}


	public boolean isRmi() {
		return rmi;
	}


	public void setRmi(boolean rmi) {
		this.rmi = rmi;
	}


	public boolean isOtherWesternNorthPacific() {
		return otherWesternNorthPacific;
	}


	public void setOtherWesternNorthPacific(boolean otherWesternNorthPacific) {
		this.otherWesternNorthPacific = otherWesternNorthPacific;
	}


	public boolean isSouthPacific() {
		return southPacific;
	}


	public void setSouthPacific(boolean southPacific) {
		this.southPacific = southPacific;
	}


	public boolean isAmericanSamoa() {
		return americanSamoa;
	}


	public void setAmericanSamoa(boolean americanSamoa) {
		this.americanSamoa = americanSamoa;
	}


	public boolean isAustralia() {
		return australia;
	}


	public void setAustralia(boolean australia) {
		this.australia = australia;
	}


	public boolean isCookIslands() {
		return cookIslands;
	}


	public void setCookIslands(boolean cookIslands) {
		this.cookIslands = cookIslands;
	}


	public boolean isFiji() {
		return fiji;
	}


	public void setFiji(boolean fiji) {
		this.fiji = fiji;
	}


	public boolean isFrenchPolynesia() {
		return frenchPolynesia;
	}


	public void setFrenchPolynesia(boolean frenchPolynesia) {
		this.frenchPolynesia = frenchPolynesia;
	}


	public boolean isKiribati() {
		return kiribati;
	}


	public void setKiribati(boolean kiribati) {
		this.kiribati = kiribati;
	}


	public boolean isNewZealand() {
		return newZealand;
	}


	public void setNewZealand(boolean newZealand) {
		this.newZealand = newZealand;
	}


	public boolean isPng() {
		return png;
	}


	public void setPng(boolean png) {
		this.png = png;
	}


	public boolean isSamoa() {
		return samoa;
	}


	public void setSamoa(boolean samoa) {
		this.samoa = samoa;
	}


	public boolean isSolomonIslands() {
		return solomonIslands;
	}


	public void setSolomonIslands(boolean solomonIslands) {
		this.solomonIslands = solomonIslands;
	}


	public boolean isTonga() {
		return tonga;
	}


	public void setTonga(boolean tonga) {
		this.tonga = tonga;
	}


	public boolean isTuvalu() {
		return tuvalu;
	}


	public void setTuvalu(boolean tuvalu) {
		this.tuvalu = tuvalu;
	}


	public boolean isVanuatu() {
		return vanuatu;
	}


	public void setVanuatu(boolean vanuatu) {
		this.vanuatu = vanuatu;
	}


	public boolean isOtherSouthPacific() {
		return otherSouthPacific;
	}


	public void setOtherSouthPacific(boolean otherSouthPacific) {
		this.otherSouthPacific = otherSouthPacific;
	}


	public boolean isPacificBasin() {
		return pacificBasin;
	}


	public void setPacificBasin(boolean pacificBasin) {
		this.pacificBasin = pacificBasin;
	}


	public boolean isGlobal() {
		return global;
	}


	public void setGlobal(boolean global) {
		this.global = global;
	}


	public boolean isAtmosphericData() {
		return atmosphericData;
	}


	public void setAtmosphericData(boolean atmosphericData) {
		this.atmosphericData = atmosphericData;
	}


	public boolean isOceanicData() {
		return oceanicData;
	}


	public void setOceanicData(boolean oceanicData) {
		this.oceanicData = oceanicData;
	}


	public boolean isTerrestrialData() {
		return terrestrialData;
	}


	public void setTerrestrialData(boolean terrestrialData) {
		this.terrestrialData = terrestrialData;
	}


	public boolean isDrought() {
		return drought;
	}


	public void setDrought(boolean drought) {
		this.drought = drought;
	}


	public boolean isRainfall() {
		return rainfall;
	}


	public void setRainfall(boolean rainfall) {
		this.rainfall = rainfall;
	}


	public boolean isFlooding() {
		return flooding;
	}


	public void setFlooding(boolean flooding) {
		this.flooding = flooding;
	}


	public boolean isBleaching() {
		return bleaching;
	}


	public void setBleaching(boolean bleaching) {
		this.bleaching = bleaching;
	}

	
	public boolean isOtherPhenomena() {
		return otherPhenomena;
	}


	public void setOtherPhenomena(boolean otherPhenomena) {
		this.otherPhenomena = otherPhenomena;
	}


	public boolean isGrid() {
		return grid;
	}


	public void setGrid(boolean grid) {
		this.grid = grid;
	}


	public boolean isPoint() {
		return point;
	}


	public void setPoint(boolean point) {
		this.point = point;
	}


	public boolean isPast() {
		return past;
	}


	public void setPast(boolean past) {
		this.past = past;
	}


	public boolean isCurrent() {
		return current;
	}


	public void setCurrent(boolean current) {
		this.current = current;
	}


	public boolean isFuture() {
		return future;
	}


	public void setFuture(boolean future) {
		this.future = future;
	}


	public boolean isOneMonth() {
		return oneMonth;
	}


	public void setOneMonth(boolean oneMonth) {
		this.oneMonth = oneMonth;
	}


	public boolean isThreeMonths() {
		return threeMonths;
	}


	public void setThreeMonths(boolean threeMonths) {
		this.threeMonths = threeMonths;
	}


	public boolean isSixMonths() {
		return sixMonths;
	}


	public void setSixMonths(boolean sixMonths) {
		this.sixMonths = sixMonths;
	}


	public boolean isInsitu() {
		return insitu;
	}


	public void setInsitu(boolean insitu) {
		this.insitu = insitu;
	}


	public boolean isRemote() {
		return remote;
	}


	public void setRemote(boolean remote) {
		this.remote = remote;
	}


	public boolean isStatistical() {
		return statistical;
	}


	public void setStatistical(boolean statistical) {
		this.statistical = statistical;
	}


	public boolean isDynamical() {
		return dynamical;
	}


	public void setDynamical(boolean dynamical) {
		this.dynamical = dynamical;
	}


	public boolean isHealth() {
		return health;
	}


	public void setHealth(boolean health) {
		this.health = health;
	}


	public boolean isFreshWater() {
		return freshWater;
	}


	public void setFreshWater(boolean freshWater) {
		this.freshWater = freshWater;
	}


	public boolean isEnergy() {
		return energy;
	}


	public void setEnergy(boolean energy) {
		this.energy = energy;
	}


	public boolean isTransportation() {
		return transportation;
	}


	public void setTransportation(boolean transportation) {
		this.transportation = transportation;
	}


	public boolean isPlanning() {
		return planning;
	}


	public void setPlanning(boolean planning) {
		this.planning = planning;
	}


	public boolean isSocioCultural() {
		return socioCultural;
	}


	public void setSocioCultural(boolean socioCultural) {
		this.socioCultural = socioCultural;
	}


	public boolean isAgriculture() {
		return agriculture;
	}


	public void setAgriculture(boolean agriculture) {
		this.agriculture = agriculture;
	}


	public boolean isRecreation() {
		return recreation;
	}


	public void setRecreation(boolean recreation) {
		this.recreation = recreation;
	}


	public boolean isEcological() {
		return ecological;
	}


	public void setEcological(boolean ecological) {
		this.ecological = ecological;
	}


	public boolean isOtherSector() {
		return otherSector;
	}


	public void setOtherSector(boolean otherSector) {
		this.otherSector = otherSector;
	}


	public boolean isWorksheetExists() {
		return worksheetExists;
	}


	public void setWorksheetExists(boolean worksheetExists) {
		this.worksheetExists = worksheetExists;
	}

	
	////////////////////////////////////////////////
	/// default/natural sort order - String  - names
	
	public int compareTo(Outlook ao) 
	{
	    boolean thisIsEmpty = false;
	    boolean aoIsEmpty = false; 
	    
	    if (this.getName() == null || this.getName().trim().length() == 0) thisIsEmpty = true; 
	    if (ao.getName() == null || ao.getName().trim().length() == 0) aoIsEmpty = true;
	    
	    if (thisIsEmpty && aoIsEmpty) return 0;
	    if (thisIsEmpty && !aoIsEmpty) return -1;
	    if (!thisIsEmpty && aoIsEmpty) return 1; 
	    return this.getName().compareToIgnoreCase(ao.getName());
    }

}
