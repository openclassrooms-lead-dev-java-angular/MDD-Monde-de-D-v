package com.openclassrooms.mddapi.common.validation;

import com.openclassrooms.mddapi.common.exception.InvalidPaginationException;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class ValidatingPageableResolver extends PageableHandlerMethodArgumentResolver {

    @Override
    public Pageable resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        validatePage(webRequest);
        validateSize(webRequest);
        validateSort(methodParameter, webRequest);

        return super.resolveArgument(
                methodParameter,
                mavContainer,
                webRequest,
                binderFactory
        );
    }

    private void validatePage(NativeWebRequest request) {
        String page = request.getParameter("page");

        if (page == null) {
            return;
        }

        try {
            if (Integer.parseInt(page) < 0) {
                throw new InvalidPaginationException(
                        "Page index must be greater than or equal to 0"
                );
            }
        } catch (NumberFormatException e) {
            throw new InvalidPaginationException(
                    "Page index must be a valid integer"
            );
        }
    }

    private void validateSize(NativeWebRequest request) {
        String size = request.getParameter("size");

        if (size == null) {
            return;
        }

        try {
            int value = Integer.parseInt(size);

            if (value <= 0) {
                throw new InvalidPaginationException(
                        "Page size must be greater than 0"
                );
            }

            if (value > 100) {
                throw new InvalidPaginationException(
                        "Page size must not exceed 100"
                );
            }

        } catch (NumberFormatException e) {
            throw new InvalidPaginationException(
                    "Page size must be a valid integer"
            );
        }
    }

    private void validateSort(
            MethodParameter methodParameter,
            NativeWebRequest request
    ) {
        String[] sorts = request.getParameterValues("sort");

        if (sorts == null) {
            return;
        }

        for (String sort : sorts) {
            String[] parts = sort.split(",");

            if (parts.length == 0 || parts[0].isBlank()) {
                throw new InvalidPaginationException(
                        "Invalid sort parameter"
                );
            }

            if (parts.length > 1) {
                String direction = parts[1];

                if (!direction.equalsIgnoreCase("asc")
                        && !direction.equalsIgnoreCase("desc")) {
                    throw new InvalidPaginationException(
                            "Sort direction must be 'asc' or 'desc'"
                    );
                }
            }

            if (parts.length > 2) {
                throw new InvalidPaginationException(
                        "Invalid sort parameter"
                );
            }
        }
    }
}
