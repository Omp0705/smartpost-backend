package com.om.smartpost.shipment.mapper;

import com.om.smartpost.shipment.dto.request.ShipmentDataRequest;
import com.om.smartpost.shipment.dto.response.ShipmentResponse;
import com.om.smartpost.shipment.dto.response.ShipmentTrackingEventResponse;
import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.shipment.entity.TrackingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentMapper {

    private final AddressMapper addressMapper;
    private final DropoffInfoMapper dropoffInfoMapper;
    private final ShipmentContactMapper shipmentContactMapper;

    public Shipment toEntity(ShipmentDataRequest request) {
        Shipment shipment = new Shipment();
        shipment.setServiceType(request.serviceType());
        shipment.setPickupAddress(addressMapper.toEntity(request.pickupAddress()));
        shipment.setDropoffInfo(dropoffInfoMapper.toEntity(request.dropoffInfo()));
        shipment.setAreaType(request.areaType());
        shipment.setDeliveryPriority(request.deliveryPriority());
        shipment.setWeightKg(request.weightKg());
        shipment.setFragile(request.fragile());
        shipment.setLengthCms(request.lengthCms());
        shipment.setWidthCms(request.widthCms());
        shipment.setHeightCms(request.heightCms());
        shipment.setCodAmount(request.codAmount());
        shipment.setPreferredSlot(request.preferredSlot());
        shipment.setPredictedSlot(request.predictedSlot());
        return shipment;
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getArticleBarcode(),
                shipment.getServiceType(),
                addressMapper.toResponse(shipment.getPickupAddress()),
                dropoffInfoMapper.toResponse(shipment.getDropoffInfo()),
                shipment.getSenderUser() != null ? shipment.getSenderUser().getUserId() : null,
                shipment.getReceiverUser() != null ? shipment.getReceiverUser().getUserId() : null,
                shipmentContactMapper.toResponse(shipment.getSenderDetails()),
                shipmentContactMapper.toResponse(shipment.getReceiverDetails()),
                shipment.getAreaType(),
                shipment.getDestinationPoName(),
                shipment.getDestinationPincode(),
                shipment.getOriginPoName(),
                shipment.getOriginPincode(),
                shipment.getDeliveryPriority(),
                shipment.getCurrentStatus(),
                shipment.getWeightKg(),
                shipment.isFragile(),
                shipment.isMerchandise(),
                shipment.getLengthCms(),
                shipment.getWidthCms(),
                shipment.getHeightCms(),
                shipment.getPhysicalWeightGms(),
                shipment.getVolumetricWeightGms(),
                shipment.getChargedWeightGms(),
                shipment.getCodAmount(),
                shipment.getPreferredSlot(),
                shipment.getPredictedSlot(),
                shipment.getBookingDate(),
                shipment.getDeliveryDate(),
                shipment.getBeat() != null ? shipment.getBeat().getId() : null,
                shipment.getBeat() != null ? shipment.getBeat().getBeatCode() : null,
                shipment.getPostman() != null ? shipment.getPostman().getEmployeeId() : null,
                shipment.getPostman() != null && shipment.getPostman().getUser() != null
                        ? shipment.getPostman().getUser().getFullName()
                        : null,
                shipment.getCreatedAt(),
                shipment.getUpdatedAt(),
                shipment.getTrackingHistory().stream().map(this::toTrackingEventResponse).toList()
        );
    }

    public ShipmentTrackingEventResponse toTrackingEventResponse(TrackingEvent trackingEvent) {
        return new ShipmentTrackingEventResponse(
                trackingEvent.getStatus(),
                trackingEvent.getDescription(),
                trackingEvent.getLocation(),
                trackingEvent.getTimestamp()
        );
    }
}


