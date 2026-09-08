package br.ifpb.project.denguemaps.pdmreportms.service;

import com.uber.h3core.H3Core;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Service
public class H3Service {

    private H3Core h3;

    @PostConstruct
    public void init() throws IOException {
        this.h3 = H3Core.newInstance();
    }

    public long calcularRes8(double lat, double lng) {
        return h3.latLngToCell(lat, lng, 8);
    }

    public long calcularRes6(double lat, double lng) {
        return h3.latLngToCell(lat, lng, 6);
    }
}
