#pragma once

#include "bitset-iterator.h"

#include <functional>
#include <iostream>
#include <string>

namespace ct {

template <typename T>
class bitset_view {
  using Word = std::uint64_t;
  static constexpr std::size_t WORD_SIZE = std::numeric_limits<Word>::digits;

public:
  using Value = bool;
  using Reference = bitset_reference<T>;
  using ConstReference = bitset_reference<const T>;
  using Iterator = bitset_iterator<T>;
  using ConstIterator = bitset_iterator<const T>;
  using View = bitset_view<T>;
  using ConstView = bitset_view<const T>;

  bitset_view() = default;
  bitset_view(const bitset_view&) = default;

  bitset_view& operator=(const bitset_view& other) = default;

  bitset_view(Iterator begin, Iterator end)
      : _begin(begin)
      , _end(end) {}

  operator bitset_view<const T>() const {
    return {_begin, _end};
  }

  Reference operator[](std::size_t index) const {
    return *(begin() + index);
  }

  Iterator begin() const {
    return _begin;
  }

  Iterator end() const {
    return _end;
  }

  bool all() const {
    return apply_check_operation(~Word(0));
  }

  bool any() const {
    return !apply_check_operation(0);
  }

  std::size_t count() const {
    std::size_t cnt = 0;

    Iterator pointer = begin();

    while (pointer != end()) {
      std::size_t can = std::min(WORD_SIZE, static_cast<std::size_t>(end() - pointer));

      Word word = pointer.get(can);

      cnt += std::popcount(word);

      pointer += can;
    }

    return cnt;
  }

  std::size_t size() const {
    return std::distance(_begin, _end);
  }

  bool empty() const {
    return size() == 0;
  }

  void swap(bitset_view& other) {
    std::swap(_begin, other._begin);
    std::swap(_end, other._end);
  }

  View subview(std::size_t offset = 0, std::size_t count = NPOS) const {
    if (offset >= size()) {
      return {end(), end()};
    }

    if (count <= size() - offset) {
      return {begin() + offset, begin() + offset + count};
    }

    return {begin() + offset, end()};
  }

  bitset_view set() const {
    bitset_view result = *this;

    result.apply_unary_operation([](Word&) { return ~static_cast<Word>(0); });

    return result;
  }

  bitset_view reset() const {
    bitset_view result = *this;

    result.apply_unary_operation([](Word&) { return 0; });

    return result;
  }

  bitset_view flip() const {
    bitset_view result = *this;

    result.apply_unary_operation([](Word word) { return ~word; });

    return result;
  }

  bitset_view operator&=(const ConstView& other) const {
    apply_binary_operation(std::bit_and<Word>(), other);

    return *this;
  }

  bitset_view operator|=(const ConstView& other) const {
    apply_binary_operation(std::bit_or<Word>(), other);

    return *this;
  }

  bitset_view operator^=(const ConstView& other) const {
    apply_binary_operation(std::bit_xor<Word>(), other);

    return *this;
  }

  friend std::string to_string(const bitset_view& view) {
    std::string result;
    result.resize(view.size());

    for (std::size_t i = 0; i < view.size(); i++) {
      result[i] = ((view[i]) ? '1' : '0');
    }

    return result;
  }

  friend std::ostream& operator<<(std::ostream& out, const bitset_view& view) {
    for (std::size_t i = 0; i < view.size(); i++) {
      out << ((view[i]) ? '1' : '0');
    }

    return out;
  }

  friend void swap(View& lhs, View& rhs) {
    lhs.swap(rhs);
  }

  void swap(const bitset_view& other) {
    swap(_begin, other._begin);
    swap(_end, other._end);
  }

private:
  Iterator _begin;
  Iterator _end;

  static constexpr size_t NPOS = -1;

  bool apply_check_operation(Word need_word) const {
    Iterator pointer = begin();

    std::size_t offset = pointer.index() % WORD_SIZE;

    if (offset > 0) {
      offset = std::min(WORD_SIZE - offset, static_cast<std::size_t>(end() - pointer));

      Word word = pointer.get(offset);

      Word mask = (offset == WORD_SIZE ? ~Word(0) : (Word(1) << offset) - 1);

      if (word != (need_word & mask)) {
        return false;
      }

      pointer += offset;
    }

    while (pointer + WORD_SIZE < end()) {
      Word word = pointer._bits[pointer.index() / WORD_SIZE];

      if (word != need_word) {
        return false;
      }

      pointer += WORD_SIZE;
    }

    if (pointer != end()) {
      std::size_t can = static_cast<std::size_t>(end() - pointer);

      Word word = pointer.get(can);

      Word mask = (can == WORD_SIZE ? ~Word(0) : (Word(1) << can) - 1);

      if (word != (need_word & mask)) {
        return false;
      }
    }

    return true;
  }

  template <class Function>
  void apply_unary_operation(Function operation) {
    Iterator pointer = begin();

    std::size_t offset = pointer.index() % WORD_SIZE;

    if (offset > 0) {
      offset = std::min(WORD_SIZE - offset, static_cast<std::size_t>(end() - pointer));

      Word word = pointer.get(offset);
      Word new_word = operation(word);

      pointer.set(new_word, offset);
      pointer += offset;
    }

    while (pointer + WORD_SIZE < end()) {
      Word word = pointer._bits[pointer.index() / WORD_SIZE];

      Word new_word = operation(word);

      pointer._bits[pointer.index() / WORD_SIZE] = new_word;

      pointer += WORD_SIZE;
    }

    if (pointer != end()) {
      std::size_t can = static_cast<std::size_t>(end() - pointer);

      Word word = pointer.get(can);
      Word new_word = operation(word);

      pointer.set(new_word, can);
    }
  }

  template <class Function>
  void apply_binary_operation(Function operation, const ConstView& other) const {
    auto first_pointer = begin();
    auto second_pointer = other.begin();

    while (first_pointer != end() && second_pointer != other.end()) {
      std::size_t can = std::min(WORD_SIZE, static_cast<std::size_t>(end() - first_pointer));

      Word first_word = first_pointer.get(can);
      Word second_word = second_pointer.get(can);

      first_pointer.set(operation(first_word, second_word), can);
      first_pointer += can;
      second_pointer += can;
    }
  }
};

} // namespace ct
