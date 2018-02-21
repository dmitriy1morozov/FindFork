package com.dmitriymorozov.findfork.service;

public interface OnServiceListener {
		void onNetworkJobsFinished();
		void onNetworkError(int code, String errorType, String errorDetail);
}
