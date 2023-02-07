package com.eikona.mata.util;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Component;

import com.eikona.mata.entity.Transaction;


@Component
public class ImageProcessingUtil {

	public byte[] searchTransactionImage(Transaction trans) {

		byte[] bytes = null;
		try {
			if(null!=trans.getCropimagePath()) {
				String imagePath = trans.getCropimagePath();
				InputStream inputStream = new FileInputStream(imagePath);

				bytes = IOUtils.toByteArray(inputStream);
				Base64.encodeBase64String(bytes);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	

}
