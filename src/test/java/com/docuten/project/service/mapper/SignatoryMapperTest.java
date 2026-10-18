package com.docuten.project.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.SignatoryDTO;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.enums.SignatoryStatusEnum;

@ExtendWith(MockitoExtension.class)
class SignatoryMapperTest {

    @Mock
    private SignatureProcessMapper signatureProcessMapper;

    @InjectMocks
    private SignatoryMapper mapper;

    @Test
    void toDTOMapsAllFieldsAndSignatureProcess() {
        LocalDateTime signDate = LocalDateTime.now().minusHours(2);
        LocalDateTime creationDate = LocalDateTime.now().minusDays(1);
        SignatureProcess process = new SignatureProcess();
        Signatory signatory = signatory(signDate, creationDate, process);
        SignatureProcessDTO processDTO = SignatureProcessDTO.builder().id(13L).build();
        when(signatureProcessMapper.toDTO(process)).thenReturn(processDTO);

        SignatoryDTO result = mapper.toDTO(signatory);

        assertEquals(signatory.getId(), result.getId());
        assertEquals(signatory.getName(), result.getName());
        assertEquals(signatory.getEmail(), result.getEmail());
        assertEquals(signatory.getSignOrder(), result.getSignOrder());
        assertEquals(signatory.getSignStatus(), result.getSignStatus());
        assertEquals(signDate, result.getSignDate());
        assertEquals(creationDate, result.getCreationDate());
        assertSame(processDTO, result.getSignatureProcess());
        verify(signatureProcessMapper).toDTO(process);
    }

    @Test
    void toEntityMapsFieldsAndSignatureProcess() {
        SignatureProcessDTO processDTO = SignatureProcessDTO.builder().build();
        SignatoryDTO dto = SignatoryDTO.builder()
                .id(12L)
                .name("Ada")
                .email("ada@example.com")
                .signOrder(3)
                .signStatus(SignatoryStatusEnum.PENDING)
                .signDate(LocalDateTime.now())
                .creationDate(LocalDateTime.now().minusDays(1))
                .signatureProcess(processDTO)
                .build();
        SignatureProcess process = new SignatureProcess();
        when(signatureProcessMapper.toEntity(processDTO)).thenReturn(process);

        Signatory result = mapper.toEntity(dto);

        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getSignOrder(), result.getSignOrder());
        assertEquals(dto.getSignStatus(), result.getSignStatus());
        assertEquals(dto.getSignDate(), result.getSignDate());
        assertEquals(dto.getCreationDate(), result.getCreationDate());
        assertSame(process, result.getSignatureProcess());
        verify(signatureProcessMapper).toEntity(processDTO);
    }

    @Test
    void toDTOAndToEntityReturnNullForNullInput() {
        assertNull(mapper.toDTO(null));
        assertNull(mapper.toEntity(null));
    }

    @Test
    void childMappingOmitsBackReferenceToPreventRecursiveMapping() {
        SignatureProcess process = new SignatureProcess();
        Signatory signatory = signatory(null, LocalDateTime.now(), process);

        assertNull(mapper.toDTOWithoutSignatureProcess(signatory).getSignatureProcess());
        assertNull(mapper.toEntityWithoutSignatureProcess(
                SignatoryDTO.builder().signatureProcess(SignatureProcessDTO.builder().build()).build())
                .getSignatureProcess());
    }

    private static Signatory signatory(LocalDateTime signDate, LocalDateTime creationDate,
            SignatureProcess process) {
        Signatory signatory = new Signatory();
        signatory.setId(6L);
        signatory.setName("Ada");
        signatory.setEmail("ada@example.com");
        signatory.setSignOrder(2);
        signatory.setSignStatus(SignatoryStatusEnum.PENDING);
        signatory.setSignDate(signDate);
        signatory.setCreationDate(creationDate);
        signatory.setSignatureProcess(process);
        return signatory;
    }
}
