package com.greydev.notionbackup.cloudstorage.pcloud;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pcloud.sdk.ApiClient;

import static org.junit.jupiter.api.Assertions.assertTrue;


class PCloudApiClientFactoryTest {

	@Test
	public void create_givenValidParams_returnsPresent() {
		Optional<ApiClient> result = PCloudApiClientFactory.create("some-access-token", "api.pcloud.com");
		assertTrue(result.isPresent());
	}

}
