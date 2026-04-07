#pragma once

#include <cassert>
#include <cstdint>
#include <iostream>
#include <numeric>
#include <type_traits>

namespace ct {

template <typename T>
class bitset_reference {
  using Word = std::uint64_t;
  static constexpr std::size_t WORD_SIZE = std::numeric_limits<Word>::digits;

public:
  bitset_reference(T* bits, std::size_t shift)
      : _bits(bits)
      , _mask(Word(1) << (WORD_SIZE - shift - 1)) {}

  operator bitset_reference<const T>() const {
    return {_mask, _bits};
  }

  bitset_reference(const bitset_reference&) = default;

  bitset_reference& operator=(bool bit) {
    if (bit) {
      *_bits |= _mask;
    } else {
      *_bits &= ~_mask;
    }
    return *this;
  }

  bitset_reference& flip() {
    *_bits ^= _mask;
    return *this;
  }

  operator bool() const {
    return (*_bits & _mask) != 0;
  }

private:
  T* _bits;
  std::size_t _mask;

  bitset_reference(std::size_t mask, T* bits)
      : _bits(bits)
      , _mask(mask) {}

  template <typename U>
  friend class bitset_reference;
};

} // namespace ct
