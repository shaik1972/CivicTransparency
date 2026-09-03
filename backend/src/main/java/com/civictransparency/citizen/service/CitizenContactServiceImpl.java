package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenContactResponse;
import com.civictransparency.citizen.dto.CreateCitizenContactRequest;
import com.civictransparency.citizen.entity.CitizenContact;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenContactRepository;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.UUID;

@Service
@Transactional
public class CitizenContactServiceImpl
                implements CitizenContactService {

        private final CitizenContactRepository contactRepository;
        private final CitizenProfileRepository profileRepository;
        private final EntityManager entityManager;

        public CitizenContactServiceImpl(
                        CitizenContactRepository contactRepository,
                        CitizenProfileRepository profileRepository,
                        EntityManager entityManager) {

                this.contactRepository = contactRepository;
                this.profileRepository = profileRepository;
                this.entityManager = entityManager;

        }

        @Override
        public CitizenContactResponse createContact(
                        UUID citizenId,
                        CreateCitizenContactRequest request) {

                if (contactRepository
                                .existsByCitizenProfileCitizenId(citizenId)) {

                        throw new IllegalArgumentException(
                                        "Contact already exists for citizen: "
                                                        + citizenId);
                }

                CitizenProfile profile = profileRepository.findById(citizenId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Citizen profile not found: "
                                                                + citizenId));

                CitizenContact contact = new CitizenContact(
                                profile,
                                request.encryptedPhone(),
                                request.encryptedEmail());

                entityManager.persist(contact);

                return toResponse(contact);
        }

        @Override
        @Transactional(readOnly = true)
        public CitizenContactResponse getContact(
                        UUID citizenId) {

                CitizenContact contact = contactRepository.findById(citizenId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Citizen contact not found: "
                                                                + citizenId));

                return toResponse(contact);
        }

        @Override
        public CitizenContactResponse verifyPhone(
                        UUID citizenId) {

                CitizenContact contact = getContactEntity(citizenId);

                contact.setVerifiedPhone(true);

                return toResponse(
                                contactRepository.save(contact));
        }

        @Override
        public CitizenContactResponse verifyEmail(
                        UUID citizenId) {

                CitizenContact contact = getContactEntity(citizenId);

                contact.setVerifiedEmail(true);

                return toResponse(
                                contactRepository.save(contact));
        }

        private CitizenContact getContactEntity(
                        UUID citizenId) {

                return contactRepository.findById(citizenId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Citizen contact not found: "
                                                                + citizenId));
        }

        private CitizenContactResponse toResponse(
                        CitizenContact contact) {

                return new CitizenContactResponse(
                                contact.getCitizenId(),
                                contact.isVerifiedPhone(),
                                contact.isVerifiedEmail());
        }
}