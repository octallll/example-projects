
#pragma once

#include <algorithm>
#include <cstddef>
#include <iostream>
#include <iterator>
#include <memory>
#include <type_traits>
#include <utility>

namespace ct {

template <typename T, std::size_t SMALL_SIZE>
class SocowVector {
  static_assert(std::is_copy_constructible_v<T>, "T must have a copy constructor");
  static_assert(std::is_nothrow_move_constructible_v<T>, "T must have a non-throwing move constructor");
  static_assert(std::is_copy_assignable_v<T>, "T must have a copy assignment operator");
  static_assert(std::is_nothrow_move_assignable_v<T>, "T must have a non-throwing move assignment operator");
  static_assert(std::is_nothrow_swappable_v<T>, "T must have a non-throwing swap");
  static_assert(SMALL_SIZE > 0, "SMALL_SIZE must be positive");

public:
  using ValueType = T;
  using Reference = T&;
  using ConstReference = const T&;
  using Pointer = T*;
  using ConstPointer = const T*;
  using Iterator = Pointer;
  using ConstIterator = ConstPointer;

  SocowVector()
      : _size(0)
      , _is_small(true) {}

  SocowVector(const SocowVector& other)
      : _size(other.size())
      , _is_small(other.is_small()) {
    if (other.is_small()) {
      std::uninitialized_copy_n(other.data(), size(), _small_data);
    } else {
      _big_data = other._big_data;
      _big_data->share();
    }
  }

  SocowVector(SocowVector&& other) noexcept
      : SocowVector() {
    swap(other);
  }

  SocowVector& operator=(const SocowVector& other) {
    if (this != &other) {
      *this = std::move(SocowVector(other));
    }
    return *this;
  }

  SocowVector& operator=(SocowVector&& other) noexcept {
    if (&other == this) {
      return *this;
    }

    this->~SocowVector();
    new (this) SocowVector(std::move(other));

    return *this;
  }

  void swap(SocowVector& other) noexcept {
    if (this == &other) {
      return;
    }

    // small <-> small
    if (is_small() && other.is_small()) {
      if (size() > other.size()) {
        other.swap(*this);
        return;
      }

      std::swap_ranges(begin(), end(), other.begin());

      std::uninitialized_move_n(other.begin() + size(), other.size() - size(), end());
      std::destroy_n(other.begin() + size(), other.size() - size());

      std::swap(_size, other._size);

      return;
    }

    // big <-> big
    if (!is_small() && !other.is_small()) {
      std::swap(_size, other._size);
      std::swap(_big_data, other._big_data);
      return;
    }

    // small <-> big
    if (is_small() && !other.is_small()) {
      BigData* copy_data = other._big_data;

      std::uninitialized_move_n(_small_data, size(), other._small_data);

      std::destroy_n(_small_data, size());

      _big_data = copy_data;
      _is_small = false;
      other._is_small = true;
      std::swap(_size, other._size);

      return;
    }

    // big <-> small
    other.swap(*this);
  }

  size_t size() const {
    return _size;
  }

  size_t capacity() const {
    return is_small() ? SMALL_SIZE : _big_data->capacity();
  }

  bool empty() const {
    return size() == 0;
  }

  Reference operator[](size_t index) {
    return data()[index];
  }

  ConstReference operator[](size_t index) const noexcept {
    return data()[index];
  }

  Reference front() {
    return data()[0];
  }

  ConstReference front() const noexcept {
    return data()[0];
  }

  Reference back() {
    return data()[size() - 1];
  }

  ConstReference back() const noexcept {
    return data()[size() - 1];
  }

  Pointer data() {
    if (is_small()) {
      return _small_data;
    }

    if (_big_data->is_shared()) {
      unshare();
    }

    return _big_data->_data;
  }

  ConstPointer data() const noexcept {
    return is_small() ? _small_data : _big_data->_data;
  }

  Iterator begin() {
    return data();
  }

  Iterator end() {
    return data() + size();
  }

  ConstIterator begin() const noexcept {
    return data();
  }

  ConstIterator end() const noexcept {
    return begin() + size();
  }

  bool is_single() {
    return is_small() || !_big_data->is_shared();
  }

  void push_back(T&& value) {
    abstract_push_back(std::move(value));
  }

  void push_back(const T& value) {
    abstract_push_back(value);
  }

  template <typename U>
  Iterator insert(ConstIterator pos, U&& value) {
    size_t index = pos - const_begin();

    push_back(std::forward<U>(value));

    std::rotate(const_begin() + index, const_end() - 1, const_end());

    return const_data() + index;
  }

  void pop_back() {
    if (!empty()) {
      if (!is_small() && _big_data->is_shared()) {
        BigData* new_data = allocate_data(capacity());

        try {
          std::uninitialized_copy_n(_big_data->_data, size() - 1, new_data->_data);
        } catch (...) {
          operator delete(new_data, std::align_val_t(alignof(T)));
          throw;
        }

        _big_data->unshare();
        _big_data = new_data;
      } else {
        (data() + size() - 1)->~T();
      }

      _size--;
    }
  }

  Iterator erase(ConstIterator pos) {
    return erase(pos, pos + 1);
  }

  Iterator erase(ConstIterator first, ConstIterator last) {
    size_t first_pos = first - const_begin();
    size_t last_pos = last - const_begin();

    if (last_pos < first_pos) {
      return nullptr;
    }

    if (!is_small() && _big_data->is_shared()) {
      SocowVector result;
      result.reserve(size() + first_pos - last_pos);

      for (size_t i = 0; i < first_pos; ++i) {
        result.push_back(const_data()[i]);
      }

      for (size_t i = last_pos; i < size(); ++i) {
        result.push_back(const_data()[i]);
      }

      result.swap(*this);

      return data() + first_pos;
    } else {
      Iterator result = data() + first_pos;

      for (size_t new_index = first_pos, index = last_pos; index < size(); index++, new_index++) {
        std::swap(data()[new_index], data()[index]);
      }

      for (size_t i = 0; i < last_pos - first_pos; i++) {
        pop_back();
      }

      return result;
    }
  }

  void clear() {
    if (is_small() || !_big_data->is_shared()) {
      std::destroy_n(data(), size());
    } else {
      _big_data->unshare();
      _big_data = nullptr;
      _is_small = true;
    }

    _size = 0;
  }

  void reserve(size_t new_capacity) {
    if (new_capacity <= SMALL_SIZE && !is_small()) {
      to_small();
      return;
    }

    if (new_capacity < capacity() || (new_capacity == capacity() && is_single())) {
      return;
    }

    to_big(new_capacity);
  }

  void shrink_to_fit() {
    if (is_small() || size() == capacity()) {
      return;
    }

    if (size() <= SMALL_SIZE) {
      to_small();
    } else if (!is_single() || size() < capacity()) {
      to_big(size());
    }
  }

  ~SocowVector() {
    if (is_small()) {
      std::destroy_n(_small_data, size());
    } else {
      if (_big_data->is_shared()) {
        _big_data->unshare();
      } else {
        std::destroy_n(_big_data->_data, size());
        operator delete(_big_data, std::align_val_t(alignof(T)));
      }
    }
  }

private:
  struct BigData {
    BigData() {}

    bool is_shared() {
      return _reference_count > 1;
    }

    void share() {
      _reference_count++;
    }

    void unshare() {
      _reference_count--;
    }

    size_t capacity() {
      return _capacity;
    }

    size_t _capacity;
    size_t _reference_count;
    T _data[0];
  };

  BigData* allocate_data(size_t capacity) {
    BigData* data = new (operator new(sizeof(BigData) + capacity * sizeof(T), std::align_val_t(alignof(T)))) BigData();
    data->_capacity = capacity;
    data->_reference_count = 1;

    return data;
  }

  void to_small() {
    BigData* data_copy = _big_data;
    _big_data = nullptr;

    try {
      if (data_copy->is_shared()) {
        std::uninitialized_copy_n(data_copy->_data, size(), _small_data);
        data_copy->unshare();
      } else {
        std::uninitialized_move_n(data_copy->_data, size(), _small_data);
        std::destroy_n(data_copy->_data, size());
        operator delete(data_copy, std::align_val_t(alignof(T)));
      }
    } catch (...) {
      _big_data = data_copy;
      throw;
    }

    _is_small = true;
  }

  void to_big(size_t new_capacity) {
    BigData* new_data = allocate_data(new_capacity);

    try {
      if (is_single()) {
        std::uninitialized_move_n(const_data(), size(), new_data->_data);
      } else {
        std::uninitialized_copy_n(const_data(), size(), new_data->_data);
      }
    } catch (...) {
      operator delete(new_data, std::align_val_t(alignof(T)));
      throw;
    }

    data_clear();
    _big_data = new_data;
    _is_small = false;
  }

  template <typename U>
  void abstract_push_back(U&& value) {
    if (size() < capacity() && (is_small() || !_big_data->is_shared())) {
      new (const_data() + size()) T(std::forward<U>(value));
      _size++;
    } else {
      size_t new_capacity = size() < capacity() ? capacity() : capacity() * 2;
      BigData* new_data = allocate_data(new_capacity);

      try {
        new (new_data->_data + size()) T(std::move(value));
      } catch (...) {
        operator delete(new_data, std::align_val_t(alignof(T)));
        throw;
      }

      try {
        if (is_single()) {
          std::uninitialized_move_n(const_data(), size(), new_data->_data);
        } else {
          std::uninitialized_copy_n(const_data(), size(), new_data->_data);
        }
      } catch (...) {
        (new_data->_data + size())->~T();
        operator delete(new_data, std::align_val_t(alignof(T)));
        throw;
      }

      data_clear();
      _big_data = new_data;
      _is_small = false;
      _size++;
    }
  }

  explicit SocowVector(size_t capacity)
      : _size(0)
      , _is_small(false)
      , _big_data(allocate_data(capacity)) {}

  void unshare() {
    if (!is_small() && _big_data->is_shared()) {
      BigData* new_data = allocate_data(_big_data->_capacity);

      try {
        std::uninitialized_copy_n(_big_data->_data, size(), new_data->_data);
      } catch (...) {
        operator delete(new_data, std::align_val_t(alignof(T)));
        throw;
      }

      _big_data->unshare();
      _big_data = new_data;
    }
  }

  Pointer const_data() noexcept {
    if (is_small()) {
      return _small_data;
    } else {
      return _big_data->_data;
    }
  }

  Pointer const_begin() noexcept {
    return const_data();
  }

  Pointer const_end() noexcept {
    return const_begin() + size();
  }

  void data_clear() {
    if (is_small()) {
      std::destroy_n(_small_data, size());
    } else {
      if (_big_data->is_shared()) {
        _big_data->unshare();
      } else {
        std::destroy_n(_big_data->_data, size());
        operator delete(_big_data, std::align_val_t(alignof(T)));
      }
    }
  }

  bool is_small() const {
    return _is_small;
  }

  size_t _size;
  bool _is_small;

  union {
    T _small_data[SMALL_SIZE];
    BigData* _big_data;
  };
};

} // namespace ct