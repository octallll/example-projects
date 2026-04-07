#include "bitset.h"

#include <iostream>

namespace ct {

BitSet::BitSet()
    : _data(nullptr)
    , _size(0) {}

BitSet::BitSet(std::size_t size)
    : _data(nullptr)
    , _size(size) {
  if (capacity() > 0) {
    _data = new Word[capacity()];
  }
}

BitSet::BitSet(std::size_t size, bool value)
    : BitSet(size) {
  for (std::size_t data_index = 0; data_index < capacity(); data_index++) {
    if (value) {
      _data[data_index] = ~static_cast<Word>(0);
    } else {
      _data[data_index] = static_cast<Word>(0);
    }
  }
}

BitSet::BitSet(const BitSet& other)
    : BitSet(other.begin(), other.end()) {}

BitSet::BitSet(std::string_view str)
    : BitSet(str.size()) {
  for (std::size_t i = 0; i < str.size(); ++i) {
    (*this)[i] = (str[i] == '1');
  }
}

BitSet::BitSet(const ConstView& other)
    : BitSet(other.begin(), other.end()) {}

BitSet::BitSet(ConstIterator first, ConstIterator last)
    : BitSet(last - first, false) {
  *this |= ConstView(first, last);
}

BitSet& BitSet::operator=(const BitSet& other) & {
  if (*this == other) {
    return *this;
  }

  BitSet bitset(other);
  swap(bitset);

  return *this;
}

BitSet& BitSet::operator=(std::string_view str) & {
  BitSet bitset(str);
  swap(bitset);

  return *this;
}

BitSet& BitSet::operator=(const ConstView& other) & {
  BitSet bitset(other);
  swap(bitset);

  return *this;
}

BitSet::~BitSet() {
  delete[] _data;
}

void BitSet::swap(ct::BitSet& other) noexcept {
  std::swap(_data, other._data);
  std::swap(_size, other._size);
}

std::size_t BitSet::size() const {
  return _size;
}

bool BitSet::empty() const {
  return size() == 0;
}

BitSet::Reference BitSet::operator[](std::size_t index) {
  return {_data + (index / WORD_SIZE), index % WORD_SIZE};
}

BitSet::ConstReference BitSet::operator[](std::size_t index) const {
  return {_data + (index / WORD_SIZE), index % WORD_SIZE};
}

BitSet::Iterator BitSet::begin() {
  return {_data, 0};
}

BitSet::ConstIterator BitSet::begin() const {
  return {_data, 0};
}

BitSet::Iterator BitSet::end() {
  return begin() + size();
}

BitSet::ConstIterator BitSet::end() const {
  return begin() + size();
}

BitSet& BitSet::operator&=(const ConstView& other) & {
  View(*this) &= other;

  return *this;
}

BitSet& BitSet::operator|=(const ConstView& other) & {
  View(*this) |= other;

  return *this;
}

BitSet& BitSet::operator^=(const ConstView& other) & {
  View(*this) ^= other;

  return *this;
}

BitSet& BitSet::operator<<=(std::size_t count) & {
  BitSet ans(size() + count, false);
  ans.subview(0, size()) |= *this;
  swap(ans);
  return *this;
}

BitSet& BitSet::operator>>=(std::size_t count) & {
  _size = (count >= size()) ? 0 : size() - count;
  return *this;
}

BitSet operator&(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs) {
  BitSet result(lhs);

  result &= rhs;

  return result;
}

BitSet operator|(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs) {
  BitSet result(lhs);

  result |= rhs;

  return result;
}

BitSet operator^(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs) {
  BitSet result(lhs);

  result ^= rhs;

  return result;
}

BitSet operator~(const BitSet::ConstView& other) {
  BitSet result(other);

  return result.flip();
}

BitSet operator<<(const BitSet::ConstView& bs, std::size_t count) {
  BitSet result(bs);

  result <<= count;

  return result;
}

BitSet operator>>(const BitSet::ConstView& bs, std::size_t count) {
  BitSet result(bs);

  result >>= count;

  return result;
}

BitSet& BitSet::flip() & {
  View(*this).flip();

  return *this;
}

BitSet& BitSet::set() & {
  View(*this).set();

  return *this;
}

BitSet& BitSet::reset() & {
  View(*this).reset();

  return *this;
}

bool BitSet::all() const {
  return ConstView(*this).all();
}

bool BitSet::any() const {
  return ConstView(*this).any();
}

std::size_t BitSet::count() const {
  return ConstView(*this).count();
}

BitSet::operator ConstView() const {
  return {begin(), end()};
}

BitSet::operator View() {
  return {begin(), end()};
}

BitSet::View BitSet::subview(std::size_t offset, std::size_t count) {
  return View(*this).subview(offset, count);
}

BitSet::ConstView BitSet::subview(std::size_t offset, std::size_t count) const {
  return ConstView(*this).subview(offset, count);
}

std::string to_string(const BitSet& bitset) {
  return to_string(bitset.subview(0, bitset.size()));
}

std::ostream& operator<<(std::ostream& out, const BitSet& bitset) {
  return out << bitset.subview(0, bitset.size());
}

bool operator==(const BitSet::ConstView& lhs, const BitSet::ConstView& rhs) {
  if (lhs.size() != rhs.size()) {
    return false;
  }

  auto first_pointer = lhs.begin();
  auto second_pointer = rhs.begin();

  while (first_pointer != lhs.end() && second_pointer != rhs.end()) {
    std::size_t can = std::min(BitSet::WORD_SIZE, static_cast<std::size_t>(lhs.end() - first_pointer));

    if (first_pointer.get(can) != second_pointer.get(can)) {
      return false;
    }

    first_pointer += can;
    second_pointer += can;
  }

  return true;
}

bool operator!=(const BitSet::ConstView& left, const BitSet::ConstView& right) {
  return !(left == right);
}

std::size_t BitSet::capacity() const {
  return (size() + WORD_SIZE - 1) / WORD_SIZE;
}

void swap(BitSet& lhs, BitSet& rhs) noexcept {
  lhs.swap(rhs);
}

} // namespace ct
