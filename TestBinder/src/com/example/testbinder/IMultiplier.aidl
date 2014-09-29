package com.example.testbinder;

import com.example.testbinder.AidlObj;

interface IMultiplier{

	void multiply(int a, int b);
	
	int send(in AidlObj obj);
}


