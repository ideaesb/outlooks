package org.ideademo.outlooks.pages.outlook;


import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;
import org.ideademo.outlooks.entities.Outlook;


public class ViewOutlook
{
	
  @PageActivationContext 
  @Property
  private Outlook entity;
  
  
  void onPrepareForRender()  {if(this.entity == null){this.entity = new Outlook();}}
  void onPrepareForSubmit()  {if(this.entity == null){this.entity = new Outlook();}}
}
