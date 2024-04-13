package es.um.sisdist.models;

import java.util.List;

public class AllConvsDTOUtils {
    public static List<ConvSummaryDTO> fromDTO(AllConvsDTO acdto) {
        return acdto.getAllConvs();
    }

    public static AllConvsDTO toDTO(List<ConvSummaryDTO> convList) {
        return new AllConvsDTO(convList);
    }
}
