package com.savantlabs.adapters.business;

import com.savantlabs.adapters.enums.SyncIssueAdapterType;
import com.savantlabs.adapters.service.SyncIssueAdapterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory class for providing the appropriate RepositoryAdapterService.
 **/
@Service
public class SyncIssuesAdapterFactory {
    private final Map<SyncIssueAdapterType, SyncIssueAdapterService> adapterServiceMap;

    /**
     * Initializes the factory with a list of available repository adapter services.
     */
    public SyncIssuesAdapterFactory(List<SyncIssueAdapterService> syncIssueAdapterServiceList) {
        adapterServiceMap = syncIssueAdapterServiceList.stream()
                .collect(Collectors.toUnmodifiableMap(SyncIssueAdapterService::getRepository, Function.identity()));
    }

    /**
     * Returns the repository adapter service for the given adapter type.
     */
    public Optional<SyncIssueAdapterService> getRepositoryAdapterService(SyncIssueAdapterType syncIssueAdapterType) {
        return Optional.ofNullable(adapterServiceMap.get(syncIssueAdapterType));
    }
}
