package com.greydev.notionbackup.cloudstorage.pcloud;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PCloudClientTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ApiClient apiClient;

	@Test
	public void upload_success() throws IOException, ApiError {
		File fileToUpload = new File("src/test/resources/testFileToUpload.txt");
		PCloudClient client = new PCloudClient(apiClient, 12345L);
		when(apiClient.createFile(anyLong(), anyString(), any()).execute()).thenReturn(null);

		boolean result = client.upload(fileToUpload);

		assertTrue(result);
	}

	@Test
	public void upload_givenIoException_returnsFalse() throws IOException, ApiError {
		File fileToUpload = new File("src/test/resources/testFileToUpload.txt");
		PCloudClient client = new PCloudClient(apiClient, 12345L);
		when(apiClient.createFile(anyLong(), anyString(), any()).execute()).thenThrow(IOException.class);

		boolean result = client.upload(fileToUpload);

		assertFalse(result);
	}

	@Test
	public void upload_givenApiError_returnsFalse() throws IOException, ApiError {
		File fileToUpload = new File("src/test/resources/testFileToUpload.txt");
		PCloudClient client = new PCloudClient(apiClient, 12345L);
		when(apiClient.createFile(anyLong(), anyString(), any()).execute()).thenThrow(mock(ApiError.class));

		boolean result = client.upload(fileToUpload);

		assertFalse(result);
	}

}
