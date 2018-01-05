package com.dmitriymorozov.findfork.ui;

public interface OnServiceWorkFinished {
		void onWorkFinished();
		void onError(int code, String errorType, String errorDetail);
}
