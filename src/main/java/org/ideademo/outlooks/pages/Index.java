package org.ideademo.outlooks.pages;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.tapestry5.PersistenceConstants;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;

import org.apache.tapestry5.corelib.components.Zone;

import org.apache.tapestry5.hibernate.HibernateSessionManager;

import org.apache.tapestry5.ioc.annotations.Inject;

import org.apache.tapestry5.services.Request;

import org.hibernate.Session;

import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;

import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import org.ideademo.outlooks.entities.Outlook;

import org.apache.log4j.Logger;


public class Index 
{
	 
  private static Logger logger = Logger.getLogger(Index.class);

  
  /////////////////////////////
  //  Drives QBE Search
  @Persist (PersistenceConstants.FLASH)
  private Outlook example;
  
  
  //////////////////////////////
  // Used in rendering Grid Row
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

  /*
  @InjectComponent
  private Zone editZone;

  @Inject
  private Request request;	
 
  @InjectPage
  private Index index;
  */
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Select Boxes - Enumaration values - the user-visible labels are externalized in Index.properties 
  //
  
  
  // the regions select box
  @Property
  @Persist
  private Regions regions;
  
  public enum Regions
  {
	 // BAS = Pacific Basin, GLB = global - see the properties file 
	 CNP, WNP, SP, BAS, GLB
  }
  
  // the ECV select box
  @Property
  @Persist
  private Ecv ecv;
  
  public enum Ecv
  {
	  // Atmospheric, Oceanic, Terrestrial
	  ATM, OCE, TER
  }
  
  // the ECV select box
  @Property
  @Persist
  private Phenomena phenomena;
  
  public enum Phenomena
  {
	  DROUGHT, RAIN, FLOOD, BLEACH, OTHERP
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
	
	// then makes lists and sublists as per the search criteria 
	List<Outlook> xlst=null;
    if(example != null)
    {
       Example ex = Example.create(example).excludeFalse().ignoreCase().enableLike(MatchMode.ANYWHERE);
       
       xlst = session.createCriteria(Outlook.class).add(ex).list();
       
       
       if (xlst != null) logger.info("Example Search Result List Size  = " + xlst.size() );
       else logger.info("Example Search result Null");
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
       org.apache.lucene.search.Query luceneQuery = qb
			    .keyword()
			    .onFields("code","name","description", "keywords","contact")
			    .matching(searchText)
			    .createQuery();
      	  
       tlst = fullTextSession.createFullTextQuery(luceneQuery, Outlook.class).list();
       if (tlst != null) logger.info("TEXT Search Result List Size  = " + tlst.size() );
       else logger.info("TEXTSearch result was Null");
    }
    
    
    // organize what type of list is returned...either total, partial (subset) or intersection of various search results  
    if (example == null && (searchText == null || searchText.trim().length() == 0))
    {
    	// Everything...
    	List <Outlook> alst = session.createCriteria(Outlook.class).list();
    	if (alst != null && alst.size() > 0)	logger.info ("Returing all " + alst.size() + " records");
    	else logger.info("No records found in the database");
        return alst;
    }
    else if (xlst == null && tlst != null)
    {
    	// just text search results
    	logger.info("Returing " + tlst.size() + " records from PURE text search");
    	return tlst;
    }
    else if (xlst != null && tlst == null)
    {
    	// just example query results
    	logger.info("Returning " + xlst.size() + " records from PURE Query-By-Example (QBE)");
    	return xlst;
    }
    else 
    {
    	// get the intersection of the two lists
    	
    	// if one of them is empty, return the other
    	if (xlst.size() == 0 && tlst.size() > 0) return tlst;
    	if (tlst.size() == 0 && xlst.size() > 0) return xlst;
    	
    	
    	List <Outlook> ivec = new Vector<Outlook>();
    	// if both are empty, return this vector. 
    	if (xlst.size() == 0 && tlst.size() == 0) return ivec; 
    	
    	// now deal with BOTH text and QBE being non-empty lists - by Id
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
  
  // ECV select box listener
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

}