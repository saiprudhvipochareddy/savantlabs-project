package com.savantlabs.adapters.business;

import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.service.RepositoryAdapterService;
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
public class RepositoryAdapterFactory {
    private final Map<RepositoryAdapterType, RepositoryAdapterService> adapterServiceMap;

    /**
     * Initializes the factory with a list of available repository adapter services.
     */
    public RepositoryAdapterFactory(List<RepositoryAdapterService> repositoryAdapterServiceList) {
        adapterServiceMap = repositoryAdapterServiceList.stream()
                .collect(Collectors.toUnmodifiableMap(RepositoryAdapterService::getRepository, Function.identity()));
    }

    /**
     * Returns the repository adapter service for the given adapter type.
     */
    public Optional<RepositoryAdapterService> getRepositoryAdapterService(RepositoryAdapterType repositoryAdapterType) {
        return Optional.ofNullable(adapterServiceMap.get(repositoryAdapterType));
    }
}
