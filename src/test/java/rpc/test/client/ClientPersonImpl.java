package rpc.test.client;


import java.util.UUID;

import javax.swing.plaf.FileChooserUI;

import rpc.test.interfaces.Person;

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

	public String name() {
		return "苍井空";
	}
	
}
