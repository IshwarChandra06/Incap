package com.eikona.mata.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.eikona.mata.util.ExportWatchlistDirectoryToExcel;

@Controller
public class WatchlistController {
	
	@Autowired
	private ExportWatchlistDirectoryToExcel exportWatchlistDirectoryToExcel;  
	
	@GetMapping("/import/watchlist-root-directory")
	public String importWatchlistImage() {
		return "multipartfile/uploadWatchlistImageDirectory";
	}
	
	@RequestMapping(value="/api/watchlist-image/export-to-file",method = RequestMethod.GET)
	public void exportToFile(HttpServletResponse response, String path,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_Shift" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportWatchlistDirectoryToExcel.exportWatchlistImageToExcel(response,path);
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
