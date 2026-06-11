package com.vin.decoder.service.external;

import com.vin.decoder.model.CarInfo;

public interface VinDecoderAdapter {
    CarInfo decodeVin(String vin);
}
