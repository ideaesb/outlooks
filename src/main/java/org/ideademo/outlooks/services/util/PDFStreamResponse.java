package org.ideademo.outlooks.services.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;

public class PDFStreamResponse implements StreamResponse {
        private InputStream is;
        private String filename="default";

        public PDFStreamResponse(InputStream is, String... args) {
                this.is = is;
                if (args != null) {
                        this.filename = args[0];
                }
        }

        public String getContentType() {
                return "application/pdf";
        }

        public InputStream getStream() throws IOException {
                return is;
        }

        public void prepareResponse(Response arg0) {
                arg0.setHeader("Content-Disposition", "attachment; filename="
                                + filename + ".pdf");
        }
}