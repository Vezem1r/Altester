package com.altester.core.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheablePage<T> implements Page<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> content = new ArrayList<>();

    @JsonIgnore
    private transient Pageable pageable;

    private int pageNumber;
    private int pageSize;
    private List<SortOrder> sortOrders = new ArrayList<>();
    private long total;

    public CacheablePage() {}

    public CacheablePage(List<T> content, int pageNumber, int pageSize,
                         List<SortOrder> sortOrders, long total) {
        this.content = content != null ? content : new ArrayList<>();
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
        this.total = total;
    }

    public CacheablePage(Page<T> page) {
        this.content = new ArrayList<>(page.getContent());
        this.total = page.getTotalElements();

        if (page.getPageable().isPaged()) {
            this.pageNumber = page.getPageable().getPageNumber();
            this.pageSize = page.getPageable().getPageSize();
            this.sortOrders = buildSortOrders(page);
        } else {
            this.pageNumber = 0;
            this.pageSize = content.size();
        }
    }

    private List<SortOrder> buildSortOrders(Page<T> page) {
        if (page.getPageable().getSort().isSorted()) {
            return StreamSupport.stream(page.getPageable().getSort().spliterator(), false)
                    .map(order -> new SortOrder(order.getProperty(), order.getDirection().name()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public Pageable getPageable() {
        if (this.pageable == null) {
            this.pageable = PageRequest.of(
                    pageNumber,
                    pageSize > 0 ? pageSize : 10,
                    buildSort()
            );
        }
        return this.pageable;
    }

    private Sort buildSort() {
        if (sortOrders == null || sortOrders.isEmpty()) {
            return Sort.unsorted();
        }
        return Sort.by(sortOrders.stream()
                .map(order -> new Sort.Order(
                        Sort.Direction.valueOf(order.getDirection()),
                        order.getProperty()))
                .toList());
    }

    @Override
    public int getTotalPages() {
        return getSize() == 0 ? 1 : (int) Math.ceil((double) total / getSize());
    }

    @Override
    public long getTotalElements() {
        return total;
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> result = new ArrayList<>();
        for (T item : content) {
            result.add(converter.apply(item));
        }

        return new CacheablePage<>(result, pageNumber, pageSize, sortOrders, total);
    }

    @Override
    public int getNumber() {
        return pageNumber;
    }

    @Override
    public int getSize() {
        return pageSize > 0 ? pageSize : content.size();
    }

    @Override
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public Sort getSort() {
        if (sortOrders == null || sortOrders.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (SortOrder sortOrder : sortOrders) {
            orders.add(new Sort.Order(
                    Sort.Direction.valueOf(sortOrder.getDirection()),
                    sortOrder.getProperty()
            ));
        }
        return Sort.by(orders);
    }

    @Override
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    public boolean isLast() {
        return !hasNext();
    }

    @Override
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }

    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    public Pageable nextPageable() {
        return hasNext() ? PageRequest.of(getNumber() + 1, getSize(), getSort()) : Pageable.unpaged();
    }

    @Override
    public Pageable previousPageable() {
        return hasPrevious() ? PageRequest.of(getNumber() - 1, getSize(), getSort()) : Pageable.unpaged();
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SortOrder implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String property;
        private String direction;

        public SortOrder() {
        }

        public SortOrder(String property, String direction) {
            this.property = property;
            this.direction = direction;
        }
    }
}