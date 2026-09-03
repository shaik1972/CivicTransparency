package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenContactResponse;
import com.civictransparency.citizen.dto.CreateCitizenContactRequest;

import java.util.UUID;

public interface CitizenContactService {

    CitizenContactResponse createContact(
            UUID citizenId,
            CreateCitizenContactRequest request);

    CitizenContactResponse getContact(UUID citizenId);

    CitizenContactResponse verifyPhone(UUID citizenId);

    CitizenContactResponse verifyEmail(UUID citizenId);
}
