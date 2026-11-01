
package com.openclassrooms.mddapi.common.validation;

import com.openclassrooms.mddapi.common.exception.InvalidPaginationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatingPageableResolverTest {

    private ValidatingPageableResolver resolver;
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        resolver = new ValidatingPageableResolver();

        methodParameter = TestController.class.getMethod("list", Pageable.class)
                .getParameters()[0] == null
                ? null
                : new MethodParameter(
                TestController.class.getMethod("list", Pageable.class),
                0
        );

        resolver.setPageParameterName("page");
        resolver.setSizeParameterName("size");
        resolver.setMaxPageSize(100);
        resolver.setOneIndexedParameters(false);
    }

    // --------------------------------------------------
    // Page validation
    // --------------------------------------------------

    @Test
    void shouldAcceptPageZero() {
        NativeWebRequest request = request("page", "0");

        Pageable pageable = resolve(request);

        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void shouldAcceptPositivePage() {
        NativeWebRequest request = request("page", "5");

        Pageable pageable = resolve(request);

        assertThat(pageable.getPageNumber()).isEqualTo(5);
    }

    @Test
    void shouldAcceptMissingPage() {
        Pageable pageable = resolve(request());

        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void shouldRejectNegativePage() {
        assertInvalidParameter(
                request("page", "-1"),
                "Page index must be greater than or equal to 0"
        );
    }

    @Test
    void shouldRejectNonNumericPage() {
        assertInvalidParameter(
                request("page", "abc"),
                "Page index must be a valid integer"
        );
    }

    // --------------------------------------------------
    // Size validation
    // --------------------------------------------------

    @Test
    void shouldAcceptPositiveSize() {
        NativeWebRequest request = request("size", "20");

        Pageable pageable = resolve(request);

        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    void shouldAcceptSizeOne() {
        NativeWebRequest request = request("size", "1");

        Pageable pageable = resolve(request);

        assertThat(pageable.getPageSize()).isEqualTo(1);
    }

    @Test
    void shouldAcceptMaximumSize() {
        NativeWebRequest request = request("size", "100");

        Pageable pageable = resolve(request);

        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void shouldAcceptMissingSize() {
        Pageable pageable = resolve(request());

        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    void shouldRejectZeroSize() {
        assertInvalidParameter(
                request("size", "0"),
                "Page size must be greater than 0"
        );
    }

    @Test
    void shouldRejectNegativeSize() {
        assertInvalidParameter(
                request("size", "-1"),
                "Page size must be greater than 0"
        );
    }

    @Test
    void shouldRejectSizeGreaterThan100() {
        assertInvalidParameter(
                request("size", "101"),
                "Page size must not exceed 100"
        );
    }

    @Test
    void shouldRejectNonNumericSize() {
        assertInvalidParameter(
                request("size", "abc"),
                "Page size must be a valid integer"
        );
    }

    // --------------------------------------------------
    // Sort validation
    // --------------------------------------------------

    @Test
    void shouldAcceptSortAscending() {
        Pageable pageable = resolve(request("sort", "title,asc"));

        assertThat(pageable.getSort())
                .contains(Sort.Order.asc("title"));
    }

    @Test
    void shouldAcceptSortDescending() {
        Pageable pageable = resolve(request("sort", "title,desc"));

        assertThat(pageable.getSort())
                .contains(Sort.Order.desc("title"));
    }

    @Test
    void shouldAcceptSortDirectionCaseInsensitive() {
        Pageable pageable = resolve(request("sort", "title,DESC"));

        assertThat(pageable.getSort())
                .contains(Sort.Order.desc("title"));
    }

    @Test
    void shouldAcceptSortWithoutDirection() {
        Pageable pageable = resolve(request("sort", "title"));

        assertThat(pageable.getSort())
                .contains(Sort.Order.asc("title"));
    }

    @Test
    void shouldAcceptMultipleSortParameters() {
        Pageable pageable = resolve(request(
                "sort", "title,asc",
                "sort", "createdAt,desc"
        ));

        assertThat(pageable.getSort())
                .contains(
                        Sort.Order.asc("title"),
                        Sort.Order.desc("createdAt")
                );
    }

    @Test
    void shouldAcceptMissingSort() {
        Pageable pageable = resolve(request());

        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }

    @Test
    void shouldRejectEmptySortProperty() {
        assertInvalidParameter(
                request("sort", ""),
                "Invalid sort parameter"
        );
    }

    @Test
    void shouldRejectBlankSortProperty() {
        assertInvalidParameter(
                request("sort", "   "),
                "Invalid sort parameter"
        );
    }

    @Test
    void shouldRejectInvalidSortDirection() {
        assertInvalidParameter(
                request("sort", "title,invalid"),
                "Sort direction must be 'asc' or 'desc'"
        );
    }

    @Test
    void shouldRejectSortWithMoreThanTwoParts() {
        assertInvalidParameter(
                request("sort", "title,asc,ignore"),
                "Invalid sort parameter"
        );
    }

    // --------------------------------------------------
    // Combined parameters
    // --------------------------------------------------

    @Test
    void shouldAcceptAllValidParametersTogether() {
        NativeWebRequest request = request(
                "page", "2",
                "size", "30",
                "sort", "title,desc"
        );

        Pageable pageable = resolve(request);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(30);
        assertThat(pageable.getSort())
                .contains(Sort.Order.desc("title"));
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    private Pageable resolve(NativeWebRequest request) {
        return resolver.resolveArgument(
                methodParameter,
                new ModelAndViewContainer(),
                request,
                null
        );
    }

    private NativeWebRequest request(String... parameters) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        for (int i = 0; i < parameters.length; i += 2) {
            servletRequest.addParameter(parameters[i], parameters[i + 1]);
        }

        return new ServletWebRequest(servletRequest);
    }

    private void assertInvalidParameter(
            NativeWebRequest request,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> resolve(request))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessage(expectedMessage);
    }

    static class TestController {
        public void list(Pageable pageable) {
        }
    }
}
