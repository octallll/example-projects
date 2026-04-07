#pragma once

#include "bitset-iterator.h"
#include "bitset-reference.h"
#include "bitset-view.h"

#include <cstddef>
#include <cstdint>
#include <string_view>

namespace ct {

class BitSet {
public:
  using Value = bool;
  using Word = std::uint64_t;
  using Reference = bitset_reference<Word>;
  using ConstReference = bitset_reference<const Word>;
  using Iterator = bitset_iterator<Word>;
  using ConstIterator = bitset_iterator<const Word>;
  using View = bitset_view<Word>;
  using ConstView = bitset_view<const Word>;

  static constexpr std::size_t WORD_SIZE = std::numeric_limits<Word>::digits;

  static constexpr std::size_t NPOS = -1;

  BitSet();
  explicit BitSet(std::size_t size);
  BitSet(std::size_t size, bool value);
  BitSet(const BitSet& other);
  explicit BitSet(std::string_view str);
  explicit BitSet(const ConstView& other);
  BitSet(ConstIterator first, ConstIterator last);

  BitSet& operator=(const BitSet& other) &;
  BitSet& operator=(std::string_view str) &;
  BitSet& operator=(const ConstView& other) &;

  ~BitSet();

  void swap(BitSet& other) noexcept;

  std::size_t size() const;
  bool empty() const;

  Reference operator[](std::size_t index);
  ConstReference operator[](std::size_t index) const;

  Iterator begin();
  ConstIterator begin() const;

  Iterator end();
  ConstIterator end() const;

  BitSet& operator&=(const ConstView& other) &;
  BitSet& operator|=(const ConstView& other) &;
  BitSet& operator^=(const ConstView& other) &;
  BitSet& operator<<=(std::size_t count) &;
  BitSet& operator>>=(std::size_t count) &;
  BitSet& flip() &;

  BitSet& set() &;
  BitSet& reset() &;

  bool all() const;
  bool any() const;
  std::size_t count() const;

  operator ConstView() const;
  operator View();

  View subview(std::size_t offset = 0, std::size_t count = NPOS);
  ConstView subview(std::size_t offset = 0, std::size_t count = NPOS) const;

private:
  Word* _data;
  std::size_t _size;

  std::size_t capacity() const;
};

BitSet operator>>(const BitSet::ConstView& bs, std::size_t count);
BitSet operator<<(const BitSet::ConstView& bs, std::size_t count);

BitSet operator&(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs);
BitSet operator|(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs);
BitSet operator^(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs);

BitSet operator~(const BitSet::ConstView& other);

bool operator==(const BitSet::ConstView& left, const BitSet::ConstView& right);
bool operator!=(const BitSet::ConstView& left, const BitSet::ConstView& right);

void swap(BitSet& lhs, BitSet& rhs) noexcept;

std::string to_string(const BitSet& bitset);
std::ostream& operator<<(std::ostream& out, const BitSet& bitset);
} // namespace ct
