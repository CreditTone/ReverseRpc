package rpc.test.client;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import sun.misc.BASE64Encoder;

import javax.swing.plaf.FileChooserUI;

import org.apache.commons.io.FileUtils;

import rpc.test.interfaces.Person;
import sun.misc.BASE64Decoder;

public class ClientPersonImpl implements Person{

	public void makelove() {
		System.out.println("去啪啪啪");
	}

	public void eat() {
		System.out.println("去吃火锅");
	}

	public void sleep() {
		System.out.println("去碎觉");
	}

	public String name() throws Exception{
		throw new Exception("123");
	}

	@SuppressWarnings("restriction")
	@Override
	public byte[] getAbc() {
		try {
			byte[] body = FileUtils.readFileToByteArray(new File("/Users/stephen/Downloads/2.pdf"));
			//return new BASE64Encoder().encode(body);
			return body;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
