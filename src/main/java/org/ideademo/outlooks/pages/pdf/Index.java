package org.ideademo.outlooks.pages.pdf;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.StreamResponse;


public class Index {
	@InjectPage
	private org.ideademo.outlooks.pages.Index index;
	
	public StreamResponse onActivate()
    {
		return index.onSelectedFromPdf();
    }
}
