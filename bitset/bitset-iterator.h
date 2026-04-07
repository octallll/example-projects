#pragma once

#include "bitset-reference.h"

#include <cstdint>
#include <iostream>
#include <numeric>

namespace ct {

template <typename T>
class bitset_iterator {
public:
  using Word = std::uint64_t;
  static constexpr std::size_t WORD_SIZE = std::numeric_limits<Word>::digits;

  using value_type = bool;
  using reference = bitset_reference<T>;
  using pointer = void;
  using difference_type = std::ptrdiff_t;
  using iterator_category = std::random_access_iterator_tag;

  template <typename>
  friend class bitset_iterator;

  bitset_iterator() = default;

  bitset_iterator(Word* bits, std::size_t index)
      : _bits(bits)
      , _index(index) {}

  operator bitset_iterator<const T>() const {
    return {_bits, _index};
  }

  reference operator*() const {
    return bitset_reference(_bits + _index / WORD_SIZE, _index % WORD_SIZE);
  }

  reference operator[](difference_type n) const {
    return bitset_reference(_bits + (_index + n) / WORD_SIZE, (_index + n) % WORD_SIZE);
  }

  bitset_iterator& operator++() {
    _index++;
    return *this;
  }

  bitset_iterator& operator--() {
    _index--;
    return *this;
  }

  bitset_iterator operator++(int) {
    bitset_iterator result = *this;
    ++_index;
    return result;
  }

  bitset_iterator operator--(int) {
    bitset_iterator result = *this;
    --_index;
    return result;
  }

  bitset_iterator operator+(difference_type n) {
    return bitset_iterator(_bits, static_cast<std::size_t>(static_cast<difference_type>(_index) + n));
  }

  friend bitset_iterator operator+(difference_type n, const bitset_iterator& it) {
    return it + n;
  }

  bitset_iterator operator-(difference_type n) {
    return bitset_iterator(_bits, static_cast<std::size_t>(static_cast<difference_type>(_index) - n));
  }

  friend bitset_iterator operator-(difference_type n, const bitset_iterator& it) {
    return bitset_iterator(it._bits, static_cast<std::size_t>(n - static_cast<difference_type>(it._index)));
  }

  template <typename U>
  difference_type operator+(const bitset_iterator<U>& other) const {
    return static_cast<difference_type>(_index) + static_cast<difference_type>(other._index);
  }

  template <typename U>
  difference_type operator-(const bitset_iterator<U>& other) const {
    return static_cast<difference_type>(_index) - static_cast<difference_type>(other._index);
  }

  bitset_iterator operator+(difference_type n) const {
    return bitset_iterator(_bits, static_cast<std::size_t>(static_cast<difference_type>(_index) + n));
  }

  bitset_iterator operator-(difference_type n) const {
    return bitset_iterator(_bits, static_cast<std::size_t>(static_cast<difference_type>(_index) - n));
  }

  bitset_iterator& operator+=(difference_type n) {
    *this = *this + n;
    return *this;
  }

  bitset_iterator& operator-=(difference_type n) {
    *this = *this - n;
    return *this;
  }

  friend bool operator<(const bitset_iterator& lhs, const bitset_iterator& rhs) {
    return lhs._index < rhs._index;
  }

  friend bool operator>(const bitset_iterator& lhs, const bitset_iterator& rhs) {
    return rhs < lhs;
  }

  friend bool operator>=(const bitset_iterator& lhs, const bitset_iterator& rhs) {
    return !(lhs < rhs);
  }

  friend bool operator<=(const bitset_iterator& lhs, const bitset_iterator& rhs) {
    return !(lhs > rhs);
  }

  friend bool operator==(const bitset_iterator& lhs, const bitset_iterator& rhs) {
    return lhs._index == rhs._index;
  }

  friend bool operator!=(const bitset_iterator& lhs, const bitset_iterator& rhs) {
    return !(lhs == rhs);
  }

  std::size_t index() {
    return _index;
  }

  void swap(bitset_iterator& other) {
    std::swap(_bits, other._bits);
    std::swap(_index, other._index);
  }

  friend void swap(bitset_iterator& left, bitset_iterator& right) {
    left.swap(right);
  }

  Word get(std::size_t size) const {
    std::size_t word_index = _index / WORD_SIZE;
    std::size_t bit_offset = _index % WORD_SIZE;

    Word first = 0;
    Word second = 0;

    std::size_t first_len = std::min(size, WORD_SIZE - bit_offset);
    std::size_t second_len = size - first_len;

    if (first_len > 0) {
      first |= ((_bits[word_index] >> (WORD_SIZE - bit_offset - first_len)) & get_mask(first_len))
            << (size - first_len);
    }

    if (second_len > 0) {
      second |= ((_bits[word_index + 1] >> (WORD_SIZE - second_len)) & get_mask(second_len)) << (size - second_len);
    }

    return first | (first_len < WORD_SIZE ? (second >> first_len) : 0);
  }

private:
  static Word get_mask(std::size_t size) {
    if (size == WORD_SIZE) {
      return ~Word(0);
    }
    return (static_cast<Word>(1) << size) - 1;
  }

  void set(Word value, std::size_t size) const {
    std::size_t word_index = _index / WORD_SIZE;
    std::size_t bit_offset = _index % WORD_SIZE;

    std::size_t first_len = std::min(size, WORD_SIZE - bit_offset);
    std::size_t second_len = size - first_len;

    if (first_len > 0) {
      Word mask = get_mask(first_len) << (WORD_SIZE - bit_offset - first_len);
      Word shift = (value >> (size - first_len)) & get_mask(first_len);

      _bits[word_index] = (_bits[word_index] & ~mask) | (shift << (WORD_SIZE - bit_offset - first_len));
    }

    if (second_len > 0) {
      Word mask = get_mask(second_len) << (WORD_SIZE - second_len);
      Word shift = value & get_mask(second_len);

      _bits[word_index + 1] = (_bits[word_index + 1] & ~mask) | (shift << (WORD_SIZE - second_len));
    }
  }

  Word* _bits;
  std::size_t _index;

  template <typename U>
  friend class bitset_view;
};

} // namespace ct
