package org.ideademo.outlooks.pages;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tapestry5.PersistenceConstants;

import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;


import org.apache.tapestry5.hibernate.HibernateSessionManager;

import org.apache.tapestry5.ioc.annotations.Inject;


import org.hibernate.Session;

import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;

import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.ideademo.outlooks.entities.Outlook;

import org.apache.log4j.Logger;


public class Index 
{
	 
  private static Logger logger = Logger.getLogger(Index.class);

  
  /////////////////////////////
  //  Drives QBE Search
  @Persist (PersistenceConstants.FLASH)
  private Outlook example;
  
  
  //////////////////////////////////////////////////////////////
  // Used in rendering within Loop just as in Grid (Table) Row
  @SuppressWarnings("unused")
  @Property 
  private Outlook row;

    
  @Property
  @Persist (PersistenceConstants.FLASH)
  private String searchText;

  @Inject
  private Session session;
  
  @Inject
  private HibernateSessionManager sessionManager;

  @Property 
  @Persist (PersistenceConstants.FLASH)
  int retrieved; 
  @Property 
  @Persist (PersistenceConstants.FLASH)
  int total;
  
  
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Select Boxes - Enumaration values - the user-visible labels are externalized in Index.properties 
  
  
  // the regions select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Regions regions;
  
  public enum Regions
  {
	 // BAS = Pacific Basin, GLB = global - see the properties file 
	 CNP, WNP, SP, BAS, GLB
  }
  
  // the ECV select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Ecv ecv;
  
  public enum Ecv
  {
	  // Atmospheric, Oceanic, Terrestrial
	  ATM, OCE, TER
  }
  
  // the ECV select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Phenomena phenomena;
  
  public enum Phenomena
  {
	  DROUGHT, RAIN, FLOOD, BLEACH, OTHERP
  }
  
  // the sector select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Sector sector;
  
  public enum Sector
  {
	  PHS, FWR, ENE, TCC, CPD, SCR, AAF, RAT, ECO, OTS
  }
  
  // the METHOLOGY select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Method methodology;
  
  public enum Method
  {
	 INSITU, REMOTE, STATIC, DYNAMIC  
  }

  // the TIMESCALE select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Timescale timescale;
  
  public enum Timescale
  {
	 PAST, CURRENT, FUTURE1, FUTURE3, FUTURE6  
  }

  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Entity List generator - QBE, Text Search or Show All 
  //

  @SuppressWarnings("unchecked")
  public List<Outlook> getList()
  {
	
	// first interpret search criteria 
	logger.info("Search Text = " + searchText);
	if (regions != null) onValueChangedFromRegions(regions.toString());
	if (ecv != null)  onValueChangedFromEcv(ecv.toString());
	if (phenomena != null) onValueChangedFromPhenomena(phenomena.toString());
	if (sector != null) onValueChangedFromSector(sector.toString());
	if (methodology != null) onValueChangedFromMethodology(methodology.toString());
	if (timescale != null) onValueChangedFromTimescale(timescale.toString());
	
    // Get all records anyway - for showing total at bottom of presentation layer
    List <Outlook> alst = session.createCriteria(Outlook.class).list();
    total = alst.size();

	
    // then makes lists and sublists as per the search criteria 
    List<Outlook> xlst=null; // xlst = Query by Example search List
    if(example != null)
    {
       Example ex = Example.create(example).excludeFalse().ignoreCase().enableLike(MatchMode.ANYWHERE);
       
       xlst = session.createCriteria(Outlook.class).add(ex).list();
       
       
       if (xlst != null)
       {
    	   logger.info("Outlook Example Search Result List Size  = " + xlst.size() );
    	   Collections.sort(xlst);
       }
       else
       {
         logger.info("Outlook Example Search result did not find any results...");
       }
    }
    
    List<Outlook> tlst=null;
    if (searchText != null && searchText.trim().length() > 0)
    {
      FullTextSession fullTextSession = Search.getFullTextSession(sessionManager.getSession());  
      try
      {
        fullTextSession.createIndexer().startAndWait();
       }
       catch (java.lang.InterruptedException e)
       {
         logger.warn("Lucene Indexing was interrupted by something " + e);
       }
      
       QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity( Outlook.class ).get();
       
       // fields being covered by text search 
       TermMatchingContext onFields = qb
		        .keyword()
		        .onFields("code","name","description", "keywords","contact", "organization", "url", "worksheet");
       
       BooleanJunction<BooleanJunction> bool = qb.bool();
       /////// Tokenize the search string for default AND logic ///
       StringTokenizer st = new StringTokenizer(searchText);
       while (st.hasMoreElements()) {
    	   bool.must(onFields.matching(st.nextElement()).createQuery());
       }
       
       org.apache.lucene.search.Query luceneQuery = bool.createQuery();
       
       tlst = fullTextSession.createFullTextQuery(luceneQuery, Outlook.class).list();
       if (tlst != null) 
       {
    	   logger.info("TEXT Search for " + searchText + " found " + tlst.size() + " Outlooks records in database");
    	   Collections.sort(tlst);
       }
       else
       {
          logger.info("TEXT Search for " + searchText + " found nothing in Outlooks");
       }
    }
    
    
    // organize what type of list is returned...either total, partial (subset) or intersection of various search results  
    if (example == null && (searchText == null || searchText.trim().length() == 0))
    {
    	// Everything...
    	if (alst != null && alst.size() > 0)
    	{
    	  logger.info ("Returing all " + alst.size() + " Outlooks records");
          Collections.sort(alst);
    	}
    	else
    	{
    	  logger.warn("No Outlook records found in the database");
    	}
    	retrieved = total;
        return alst; 
    }
    else if (xlst == null && tlst != null)
    {
    	// just text search results
    	logger.info("Returing " + tlst.size() + " Outlooks records as a result of PURE text search (no QBE) for " + searchText);
    	retrieved = tlst.size();
    	return tlst;
    }
    else if (xlst != null && tlst == null)
    {
    	// just example query results
    	logger.info("Returning " + xlst.size() + " Outlooks records as a result of PURE Query-By-Example (QBE), no text string");
    	retrieved = xlst.size();
    	return xlst;
    }
    else 
    {

        ////////////////////////////////////////////
    	// get the INTERSECTION of the two lists
    	
    	// TRIVIAL: if one of them is empty, return the other
    	// if one of them is empty, return the other
    	if (xlst.size() == 0 && tlst.size() > 0)
    	{
          logger.info("Returing " + tlst.size() + " Outlooks records as a result of ONLY text search, QBE pulled up ZERO records for " + searchText);
          retrieved = tlst.size();
          return tlst;
    	}

    	if (tlst.size() == 0 && xlst.size() > 0)
    	{
          logger.info("Returning " + xlst.size() + " Outlooks records as a result of ONLY Query-By-Example (QBE), text search pulled up NOTHING for string " + searchText);
          retrieved = xlst.size();
          return xlst;
    	}
    	
    	
    	List <Outlook> ivec = new Vector<Outlook>();
    	// if both are empty, return this Empty vector. 
    	if (xlst.size() == 0 && tlst.size() == 0)
    	{
          logger.info("Neither QBE nor text search for string " + searchText +  " pulled up ANY Outlooks Records.");
          retrieved = 0;
          return ivec;
    	}
    	


    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// now deal with BOTH text and QBE being non-empty lists - implementing intersection by Database Primary Key -  Id
    	Iterator<Outlook> xiterator = xlst.iterator();
    	while (xiterator.hasNext()) 
    	{
          Outlook x = xiterator.next();
          Long xid = x.getId();
    		
          Iterator<Outlook> titerator = tlst.iterator();
    		while(titerator.hasNext())
    		{
        		Outlook t = titerator.next();
        		Long tid = t.getId();
    			
        		if (tid == xid)
        		{
        			ivec.add(t); break;
        		}
        		
    		}
    			
    	}
    	

        // sort again - 
    	if (ivec.size() > 0)  Collections.sort(ivec);
    	logger.info("Returning " + ivec.size() + " Outlooks records from COMBINED (text, QBE) Search");
    	retrieved = ivec.size();
    	return ivec;
    }
    
  }
  

  
  ///////////////////////////////////////////////////////////////
  //  Action Event Handlers 
  //
  
  Object onSelectedFromSearch() 
  {
    return null; 
  }

  Object onSelectedFromClear() 
  {
    this.searchText = "";
   
    // nullify selectors 
    regions=null;
    ecv=null;
    phenomena=null;
    methodology=null;
    timescale=null;
    
    this.example = null;
    return null; 
  }
  
  // regions select box listener...may be hooked-up to some AJAX zone if needed (later)
  Object onValueChangedFromRegions(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Region Select:  Example is NULL");
		  this.example = new Outlook(); 
	  }
	  else
	  {
		  logger.info("Region Select:  Example is NOT null");
	  }
	  logger.info("Region Choice = " + choice);
	  
	  clearRegions(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("CNP"))
      {
    	example.setCentralNorthPacific(true);
    	logger.info("Example setCentralNorthPacific");
      }
      else if (choice.equalsIgnoreCase("WNP"))
      {
    	example.setWesternNorthPacific(true);
      }
      else if (choice.equalsIgnoreCase("SP"))
      {
    	example.setSouthPacific(true);  
      }
      else if (choice.equalsIgnoreCase("BAS"))
      {
    	example.setPacificBasin(true);   
      }
      else if (choice.equalsIgnoreCase("GLB"))
      {
    	example.setGlobal(true);
      }
      else
      {
    	  // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
	
  // ECV select box listener
  Object onValueChangedFromEcv(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("ECV Select: Example is NULL");
		  this.example = new Outlook(); 
	  }
	  else
	  {
		  logger.info("ECV Select: Example is NOT null");
	  }
	  logger.info("ECV Choice = " + choice);
	  
	  clearEcv(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("ATM"))
      {
    	example.setAtmosphericData(true);
      }
      else if (choice.equalsIgnoreCase("OCE"))
      {
    	example.setOceanicData(true);
      }
      else if (choice.equalsIgnoreCase("TER"))
      {
    	example.setTerrestrialData(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
  // ECV Phenomena box listener
  Object onValueChangedFromPhenomena(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Phenomena Select Value Changed, Example is NULL");
		  this.example = new Outlook(); 
	  }
	  else
	  {
		  logger.info("Phenomena Select Value Changed, Example is NOT null");
	  }
	  logger.info("Phenomena Chosen = " + choice);
	  
	  clearPhenomena(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("DROUGHT"))
      {
    	example.setDrought(true);
      }
      else if (choice.equalsIgnoreCase("RAIN"))
      {
    	example.setRainfall(true);
      }
      else if (choice.equalsIgnoreCase("FLOOD"))
      {
    	example.setFlooding(true);  
      }
      else if (choice.equalsIgnoreCase("BLEACH"))
      {
    	example.setBleaching(true);  
      }
      else if (choice.equalsIgnoreCase("OTHERP"))
      {
    	example.setOtherPhenomena(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
 
  
  Object onValueChangedFromSector(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Sector Select Value Changed, Example is NULL");
		  this.example = new Outlook(); 
	  }
	  else
	  {
		  logger.info("Sector Select Value Changed, Example is NOT null");
	  }
	  logger.info("Sector Chosen = " + choice);
	  
	  clearSector(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("PHS"))
      {
    	example.setHealth(true);
      }
      else if (choice.equalsIgnoreCase("FWR"))
      {
    	example.setFreshWater(true);
      }
      else if (choice.equalsIgnoreCase("ENE"))
      {
    	example.setEnergy(true);  
      }
      else if (choice.equalsIgnoreCase("TCC"))
      {
    	example.setTransportation(true);  
      }
      else if (choice.equalsIgnoreCase("CPD"))
      {
    	example.setPlanning(true);  
      }
      else if (choice.equalsIgnoreCase("SCR"))
      {
    	example.setSocioCultural(true);  
      }
      else if (choice.equalsIgnoreCase("AAF"))   
      {
    	example.setAgriculture(true);  
      }
      else if (choice.equalsIgnoreCase("RAT"))
      {
    	example.setRecreation(true);  
      }
      else if (choice.equalsIgnoreCase("ECO"))
      {
    	example.setEcological(true);  
      }
      else if (choice.equalsIgnoreCase("OTS"))
      {
    	example.setOtherSector(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
  Object onValueChangedFromMethodology(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("METHODOLOGY search criteria was changed -  Example is NULL");
		  this.example = new Outlook(); 
	  }
	  else
	  {
		  logger.info("METHODOLOGY search criteria was changed -  Example is NOT null");
	  }
	  logger.info("Looking for Methodology = " + choice);
	  
	  clearMethodologies(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("INSITU"))
      {
    	example.setInsitu(true);
      }
      else if (choice.equalsIgnoreCase("REMOTE"))
      {
    	example.setRemote(true);
      }
      else if (choice.equalsIgnoreCase("STATIC"))
      {
    	example.setStatistical(true);  
      }
      else if (choice.equalsIgnoreCase("DYNAMIC"))
      {
    	example.setDynamical(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }

  Object onValueChangedFromTimescale(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Picked Timescale Criteria - Example is NULL");
		  this.example = new Outlook(); 
	  }
	  else
	  {
		  logger.info("Picked a timescale search criteria - Example is NOT null");
	  }
	  logger.info("Timescale search criteria  = " + choice);
	  
	  clearTimescales(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("PAST"))
      {
    	example.setPast(true);
      }
      else if (choice.equalsIgnoreCase("CURRENT"))
      {
    	example.setCurrent(true);
      }
      else if (choice.equalsIgnoreCase("FUTURE"))
      {
    	// this will never work as of Sep 6 2012 since the choice was removed choice box and hence from QBE search 
    	example.setFuture(true);  
      }
      else if (choice.equalsIgnoreCase("FUTURE1"))
      {
    	example.setOneMonth(true);  
      }
      else if (choice.equalsIgnoreCase("FUTURE3"))
      {
    	example.setThreeMonths(true);
      }
      else if (choice.equalsIgnoreCase("FUTURE6"))
      {
    	example.setSixMonths(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
  
  
  ////////////////////////////////////////////////
  //  QBE Setter 
  //  

  public void setExample(Outlook x) 
  {
    this.example = x;
  }

  
  
  ///////////////////////////////////////////////////////
  // private methods 
  
  private void clearRegions(Outlook outlook)
  {
   	outlook.setCentralNorthPacific(false);
  	outlook.setWesternNorthPacific(false);
  	outlook.setSouthPacific(false);
  	outlook.setPacificBasin(false);
  	outlook.setGlobal(false);
  }
  
  private void clearEcv(Outlook outlook)
  {
	outlook.setAtmosphericData(false);
	outlook.setOceanicData(false);
	outlook.setTerrestrialData(false);
  }
  
  private void clearPhenomena(Outlook outlook)
  {
	outlook.setDrought(false);
	outlook.setRainfall(false);
	outlook.setFlooding(false);
	outlook.setBleaching(false);
	outlook.setOtherPhenomena(false);
  }
  
  private void clearSector(Outlook outlook)
  {
	outlook.setHealth(false);
	outlook.setFreshWater(false);
	outlook.setEnergy(false);
	outlook.setTransportation(false);
	outlook.setPlanning(false);
	outlook.setSocioCultural(false);
	outlook.setAgriculture(false);
	outlook.setRecreation(false);
	outlook.setEcological(false);
	outlook.setOtherSector(false);
  }

  private void clearMethodologies(Outlook outlook)
  {
	outlook.setInsitu(false);
	outlook.setRemote(false);
	outlook.setStatistical(false);
	outlook.setDynamical(false);
  }
  
  private void clearTimescales(Outlook outlook)
  {
	 outlook.setPast(false);
	 outlook.setCurrent(false);
	 outlook.setFuture(false);
	 outlook.setOneMonth(false);
	 outlook.setThreeMonths(false);
	 outlook.setSixMonths(false);
  }
}