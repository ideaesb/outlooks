package org.ideademo.outlooks.pages;

import java.io.StringReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Path;
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

import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.ideademo.outlooks.services.util.PDFStreamResponse;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Index 
{
	 
  private static Logger logger = Logger.getLogger(Index.class);
  private static final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_31); 

  
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

  @Inject
  @Path("context:layout/images/image067.gif")
  private Asset logoAsset;
	  
  @Inject
  private AssetSource assetSource;
  
  @Inject
  Messages messages;

  
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
       TokenStream stream = analyzer.tokenStream(null, new StringReader(searchText));
       CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
       try
       {
        while (stream.incrementToken()) 
         {
    	   String token = cattr.toString();
    	   logger.info("Adding search token " +  token + " to look in Outlooks database");
    	   bool.must(onFields.matching(token).createQuery());
         }
        stream.end(); 
        stream.close(); 
       }
       catch (IOException ioe)
       {
    	   logger.warn("Outlooks Text Search: Encountered problem tokenizing search term " + searchText);
    	   logger.warn(ioe);
       }
       
       /////////////  the lucene query built from non-simplistic English words 
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
  
  public StreamResponse onSelectedFromPdf() 
  {
     String subheader = "Printing " + retrieved + " of total " + total + " records.";
     if (StringUtils.isNotBlank(searchText))
     {
    	  subheader += "  Searching for \"" + searchText + "\""; 
     }
     Document document = new Document();
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     try
     {
         PdfWriter writer = PdfWriter.getInstance(document, baos);
         document.open();
         document.add(getLogo());
         document.add(new Paragraph(getHeader("PaCIS Outlooks ")));
         document.add(new Paragraph(subheader));
         document.add(Chunk.NEWLINE);document.add(Chunk.NEWLINE);
         List<Outlook> list = getList();
         Iterator<Outlook> iterator = list.iterator();
         while (iterator.hasNext())
         {
        	 Outlook o = iterator.next();
        	 document.add(getPDFTable(o));
        	 document.add(Chunk.NEWLINE);
         }
         document.close();
     }
     catch (Exception e)
     {
   	  logger.warn("Error generating PDF Doc " + e);
     }
     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
     return new PDFStreamResponse(bais,"PaCIS_Outlooks_" + System.currentTimeMillis());
  }

  public StreamResponse onReturnStreamResponse(long id) 
  {
      Document document = new Document();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
          PdfWriter writer = PdfWriter.getInstance(document, baos);
          document.open();
          document.add(getLogo());
          document.add(new Paragraph(getHeader("PaCIS Outlook ")));
          document.add(Chunk.NEWLINE);document.add(Chunk.NEWLINE);
          Outlook o =  (Outlook) session.load(Outlook.class, id);
          document.add(getPDFTable(o));
          document.close();
      }
      catch (Exception e)
      {
    	  logger.warn("Error generating PDF Doc " + e);
      }
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return new PDFStreamResponse(bais,"PaCIS_Outlook_" + System.currentTimeMillis());
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
  private List<PdfPTable> getPDFTables(List<Outlook> list)
  {
    List <PdfPTable> tables = new Vector<PdfPTable>();
    Iterator<Outlook>iterator = list.iterator();
    while(iterator.hasNext())
    {
      tables.add(getPDFTable(iterator.next()));
    }
    return tables;
  }
  private PdfPTable getPDFTable(Outlook outlook)
  {
      // create table, 2 columns
      String acronym = StringUtils.trimToEmpty(outlook.getCode());
      String name = StringUtils.trimToEmpty(outlook.getName());
      String description = StringUtils.trimToEmpty(outlook.getDescription());
      String leadAgencies = StringUtils.trimToEmpty(outlook.getOrganization());
      String contacts = StringUtils.trimToEmpty(outlook.getContact());
      String url = StringUtils.trimToEmpty(outlook.getUrl());
  		
  		
        PdfPTable table = new PdfPTable(2);
        try
        {
          table.setWidths(new int[]{1, 4});
        }
        catch (Exception e)
        {
      	  logger.fatal("Could not setWidths???" + e );
        }
        
        table.setSplitRows(false);
        
        PdfPCell nameTitle = new PdfPCell(new Phrase("Name")); 
        
        if (StringUtils.isNotBlank(acronym)) name = name + " (" + acronym + ")";
        PdfPCell nameCell = new PdfPCell(new Phrase(name));
        
        nameTitle.setBackgroundColor(BaseColor.CYAN);  nameCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        
        table.addCell(nameTitle);  table.addCell(nameCell);          		          		
  		


  		//region
  		com.itextpdf.text.List regions = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
  		if (outlook.isCentralNorthPacific())
  		{
  			ListItem item = new ListItem(getLabel("centralNorthPacific")); regions.add(item);
  		}
  		if (outlook.isStateOfHawaii())
  		{
  			ListItem item = new ListItem(getLabel("stateOfHawaii")); regions.add(item);
  		}
  		if (outlook.isNorthWestHawaiianIslands())
  		{
  			ListItem item = new ListItem(getLabel("northWesternHawaiianIslands")); regions.add(item);
  		}
  		if (outlook.isPacificRemoteIslands())
  		{
  			ListItem item = new ListItem(getLabel("pacificRemoteIslands")); regions.add(item);
  		}
  		if (outlook.isWesternNorthPacific())
  		{
  			ListItem item = new ListItem(getLabel("westernNorthPacific")); regions.add(item);
  		}
  		if (outlook.isCnmi())
  		{
  			ListItem item = new ListItem(getLabel("cnmi")); regions.add(item);
  		}
  		if (outlook.isFsm())
  		{
  			ListItem item = new ListItem(getLabel("fsm")); regions.add(item);
  		}
  		if (outlook.isGuam())
  		{
  			ListItem item = new ListItem(getLabel("guam")); regions.add(item);
  		}
  		if (outlook.isPalau())
  		{
  			ListItem item = new ListItem(getLabel("palau")); regions.add(item);
  		}
  		if (outlook.isRmi())
  		{
  			ListItem item = new ListItem(getLabel("rmi")); regions.add(item);
  		}
  		if (outlook.isOtherWesternNorthPacific())
  		{
  			ListItem item = new ListItem(getLabel("otherWesternNorthPacific")); regions.add(item);
  		}
  		if (outlook.isSouthPacific())
  		{
  			ListItem item = new ListItem(getLabel("southPacific")); regions.add(item);
  		}
  		if (outlook.isAmericanSamoa())
  		{
  			ListItem item = new ListItem(getLabel("americanSamoa")); regions.add(item);
  		}
  		if (outlook.isAustralia())
  		{
  			ListItem item = new ListItem(getLabel("australia")); regions.add(item);
  		}
  		if (outlook.isCookIslands())
  		{
  			ListItem item = new ListItem(getLabel("cookIslands")); regions.add(item);
  		}
  		if (outlook.isFiji())
  		{
  			ListItem item = new ListItem(getLabel("fiji")); regions.add(item);
  		}
  		if (outlook.isFrenchPolynesia())
  		{
  			ListItem item = new ListItem(getLabel("frenchPolynesia")); regions.add(item);
  		}
  		if (outlook.isKiribati())
  		{
  			ListItem item = new ListItem(getLabel("kiribati")); regions.add(item);
  		}
  		if (outlook.isNewZealand())
  		{
  			ListItem item = new ListItem(getLabel("newZealand")); regions.add(item);
  		}
  		if (outlook.isPng())
  		{
  			ListItem item = new ListItem(getLabel("png")); regions.add(item);
  		}
  		if (outlook.isSamoa())
  		{
  			ListItem item = new ListItem(getLabel("samoa")); regions.add(item);
  		}
  		if (outlook.isSolomonIslands())
  		{
  			ListItem item = new ListItem(getLabel("solomonIslands")); regions.add(item);
  		}
  		if (outlook.isTonga())
  		{
  			ListItem item = new ListItem(getLabel("tonga")); regions.add(item);
  		}
  		if (outlook.isTuvalu())
  		{
  			ListItem item = new ListItem(getLabel("tuvalu")); regions.add(item);
  		}
  		if (outlook.isVanuatu())
  		{
  			ListItem item = new ListItem(getLabel("vanuatu")); regions.add(item);
  		}
  		if (outlook.isOtherSouthPacific())
  		{
  			ListItem item = new ListItem(getLabel("otherSouthPacific")); regions.add(item);
  		}
  		if (outlook.isPacificBasin())
  		{
  			ListItem item = new ListItem(getLabel("pacificBasin")); regions.add(item);
  		}
  		if (outlook.isGlobal())
  		{
  			ListItem item = new ListItem(getLabel("global")); regions.add(item);
  		}
  		
		
  		if (regions.size() > 0)
  		{
   		  PdfPCell rCell = new PdfPCell(); rCell.addElement(regions);
   		  table.addCell(new PdfPCell(new Phrase("Regions")));  table.addCell(rCell);
  		}

  	    // ecv
  		com.itextpdf.text.List ecv = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
  		if (outlook.isAtmosphericData()) 
  		{
  			ListItem item = new ListItem(getLabel("atmosphericData")); ecv.add(item);
  		}
  		if (outlook.isOceanicData()) 
  		{
  			ListItem item = new ListItem(getLabel("oceanicData")); ecv.add(item);
  		}
  		if (outlook.isTerrestrialData()) 
  		{
  			ListItem item = new ListItem(getLabel("terrestrialData")); ecv.add(item);
  		}

  		if(ecv.size() > 0)
  		{
  		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(ecv);
  		  table.addCell(new PdfPCell(new Phrase("Essential Climate Variable")));  table.addCell(pdfCell);
  		}

  		// phenomena/impacts
  		com.itextpdf.text.List impacts = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
  		if (outlook.isDrought()) 
  		{
  			ListItem item = new ListItem(getLabel("drought")); impacts.add(item);
  		}
  		if (outlook.isRainfall()) 
  		{
  			ListItem item = new ListItem(getLabel("rainfall")); impacts.add(item);
  		}
  		if (outlook.isFlooding()) 
  		{
  			ListItem item = new ListItem(getLabel("flooding")); impacts.add(item);
  		}
  		if (outlook.isBleaching()) 
  		{
  			ListItem item = new ListItem(getLabel("bleaching")); impacts.add(item);
  		}
  		if (outlook.isOtherPhenomena()) 
  		{
  			ListItem item = new ListItem(getLabel("otherPhenomena")); impacts.add(item);
  		}

  		if(impacts.size() > 0)
  		{
  		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(impacts);
  		  table.addCell(new PdfPCell(new Phrase("Phenomena/Impacts")));  table.addCell(pdfCell);
  		}
  		
  		
  		
  		// spatial/scale
  		com.itextpdf.text.List scales = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
 		
  		if (outlook.isGrid()) 
  		{
  			ListItem item = new ListItem(getLabel("grid")); scales.add(item);
  		}
  		if (outlook.isPoint()) 
  		{
  			ListItem item = new ListItem(getLabel("point")); scales.add(item);
  		}
  		
  		if(scales.size() > 0)
  		{
  		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(scales);
  		  table.addCell(new PdfPCell(new Phrase("Spatial/Scale")));  table.addCell(pdfCell);
  		}

  		// timescale
  		com.itextpdf.text.List timescale = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
  		if (outlook.isPast()) 
  		{
  			ListItem item = new ListItem(getLabel("past")); timescale.add(item);
  		}
  		if (outlook.isCurrent()) 
  		{
  			ListItem item = new ListItem(getLabel("current")); timescale.add(item);
  		}
  		if (outlook.isFuture()) 
  		{
  			ListItem item = new ListItem(getLabel("future")); timescale.add(item);
  		}
  		if (outlook.isOneMonth()) 
  		{
  			ListItem item = new ListItem(getLabel("oneMonth")); timescale.add(item);
  		}
  		if (outlook.isThreeMonths()) 
  		{
  			ListItem item = new ListItem(getLabel("threeMonths")); timescale.add(item);
  		}
  		if (outlook.isSixMonths()) 
  		{
  			ListItem item = new ListItem(getLabel("sixMonths")); timescale.add(item);
  		}

  		
  		if(timescale.size() > 0)
  		{
  		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(timescale);
  		  table.addCell(new PdfPCell(new Phrase("Time Scale")));  table.addCell(pdfCell);
  		}
  		
  		// methodologies
  		com.itextpdf.text.List methodologies = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
  		if (outlook.isInsitu()) 
  		{
  			ListItem item = new ListItem(getLabel("insitu")); methodologies.add(item);
  		}
  		if (outlook.isRemote()) 
  		{
  			ListItem item = new ListItem(getLabel("remote")); methodologies.add(item);
  		}
  		if (outlook.isStatistical()) 
  		{
  			ListItem item = new ListItem(getLabel("statistical")); methodologies.add(item);
  		}
  		if (outlook.isDynamical()) 
  		{
  			ListItem item = new ListItem(getLabel("dynamical")); methodologies.add(item);
  		}

  		
  		if(methodologies.size() > 0)
  		{
  		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(methodologies);
  		  table.addCell(new PdfPCell(new Phrase("Methodology")));  table.addCell(pdfCell);
  		}
  		
  		
  	    // sectors
  		com.itextpdf.text.List sectors = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
  		if (outlook.isHealth()) 
  		{
  			ListItem item = new ListItem(getLabel("health")); sectors.add(item);
  		}
  		if (outlook.isFreshWater()) 
  		{
  			ListItem item = new ListItem(getLabel("freshWater")); sectors.add(item);
  		}
  		if (outlook.isEnergy()) 
  		{
  			ListItem item = new ListItem(getLabel("energy")); sectors.add(item);
  		}
  		if (outlook.isTransportation()) 
  		{
  			ListItem item = new ListItem(getLabel("transportation")); sectors.add(item);
  		}
  		if (outlook.isPlanning()) 
  		{
  			ListItem item = new ListItem(getLabel("planning")); sectors.add(item);
  		}
  		if (outlook.isSocioCultural()) 
  		{
  			ListItem item = new ListItem(getLabel("socioCultural")); sectors.add(item);
  		}
  		if (outlook.isAgriculture()) 
  		{
  			ListItem item = new ListItem(getLabel("agriculture")); sectors.add(item);
  		}
  		if (outlook.isRecreation()) 
  		{
  			ListItem item = new ListItem(getLabel("recreation")); sectors.add(item);
  		}
  		if (outlook.isEcological()) 
  		{
  			ListItem item = new ListItem(getLabel("ecological")); sectors.add(item);
  		}
  		if (outlook.isOtherSector()) 
  		{
  			ListItem item = new ListItem(getLabel("otherSector")); sectors.add(item);
  		}


  		if(sectors.size() > 0)
  		{
  		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(sectors);
  		  table.addCell(new PdfPCell(new Phrase("Sectors")));  table.addCell(pdfCell);
  		}
  		
  		
  		
  		
  		if (StringUtils.isNotBlank(description))
  		{
  		  table.addCell(new PdfPCell(new Phrase("Description")));  table.addCell(new PdfPCell(new Phrase(description)));
  		}

  		if (StringUtils.isNotBlank(url))
  		{
    	          Anchor link = new Anchor(StringUtils.trimToEmpty(url)); link.setReference(StringUtils.trimToEmpty(url));
  		  table.addCell(new PdfPCell(new Phrase("Url")));  table.addCell(new PdfPCell(link));
  		}
                if (StringUtils.isNotBlank(leadAgencies))
  		{
  		  table.addCell(new PdfPCell(new Phrase("Lead Agencies")));  table.addCell(new PdfPCell(new Phrase(leadAgencies)));
  		}

  		if (StringUtils.isNotBlank(contacts))
  		{
  		  table.addCell(new PdfPCell(new Phrase("Contacts")));  table.addCell(new PdfPCell(new Phrase(contacts)));
  		}
  		
  		return table;

	}
   private String getLabel (String varName)
   {
   String key = varName + "-label";
   String value = "";
   if (messages.contains(key)) value = messages.get(key);
   else value = TapestryInternalUtils.toUserPresentable(varName);
   return StringUtils.trimToEmpty(value);
   }
   private com.itextpdf.text.Image getLogo()
   {
 	  java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(logoAsset.getResource().toURL());
 	  try
 	  {
	    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(awtImage, null);
	    logo.scalePercent(50);
	    return logo;
 	  }
 	  catch (Exception e)
 	  {
 		 logger.warn("Could not generate logo " + e);
 	  }
	  
      return null;
   }
   private String getHeader(String prefix)
   {
	      DateFormat formatter = new SimpleDateFormat
                  ("EEE MMM dd HH:mm:ss zzz yyyy");
              Date date = new Date(System.currentTimeMillis());
              TimeZone eastern = TimeZone.getTimeZone("Pacific/Honolulu");
              formatter.setTimeZone(eastern);
              
      return prefix + formatter.format(date); 
   }

}