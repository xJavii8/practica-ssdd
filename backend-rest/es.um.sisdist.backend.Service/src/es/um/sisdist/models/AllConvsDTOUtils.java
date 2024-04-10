package es.um.sisdist.models;

import java.util.List;

public class AllConvsDTOUtils {
    public static List<ConversationSummaryDTO> fromDTO(AllConvsDTO acdto) {
        return acdto.getAllConvs();
    }

    public static AllConvsDTO toDTO(List<ConversationSummaryDTO> convList) {
        return new AllConvsDTO(convList);
    }
}
