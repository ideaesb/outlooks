jQuery.noConflict();
jQuery(document).ready(function()
{
  jQuery("input:checkbox").click(function(event)
  {
	if (jQuery(this).attr('checked'))
	{
      var labelText = jQuery('label[for=' + jQuery(this).attr("id") + ']').html();
      var edit = jQuery("#keywords");
      var curValue = edit.val();
      var newValue = curValue + " " + labelText;
      edit.val(newValue);
	}
  });
});
