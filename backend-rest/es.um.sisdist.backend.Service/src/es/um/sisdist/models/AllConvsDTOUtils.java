package es.um.sisdist.models;

import java.util.List;

public class AllConvsDTOUtils {
    public static List<ConversationSummary> fromDTO(AllConvsDTO acdto) {
        return acdto.getAllConvs();
    }

    public static AllConvsDTO toDTO(List<ConversationSummary> convList) {
        return new AllConvsDTO(convList);
    }
}
