package com.creedon.reactlibrary.videotrimmer.interfaces;
/**
 * _   _ _______   ________ _       _____   __
 * | \ | |_   _\ \ / /| ___ \ |     / _ \ \ / /
 * |  \| | | |  \ V / | |_/ / |    / /_\ \ V /
 * | . ` | | |  /   \ |  __/| |    |  _  |\ /
 * | |\  |_| |_/ /^\ \| |   | |____| | | || |
 * \_| \_/\___/\/   \/\_|   \_____/\_| |_/\_/
 * <p>
 * modified by jameskong on 12/2/2019.
 */
/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/01/21 6:01 PM
 * version: 1.0
 * description:
 */
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;

public class VideoCompressListener extends ExecuteBinaryResponseHandler {

	@Override
	public void onSuccess(String message) {
	}

	@Override
	public void onFailure(String message) {
	}
}
