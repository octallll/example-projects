#pragma once

#include <cstddef>
#include <iostream>
#include <utility>

namespace ct {

template <size_t I, typename T>
struct TupleElement {
  T value;

  TupleElement() = default;

  template <typename U>
  explicit TupleElement(U&& value)
      : value(std::forward<U>(value)) {}
};

template <typename Seq, typename... Types>
struct TupleBase;

template <size_t... Is, typename... Types>
struct TupleBase<std::index_sequence<Is...>, Types...> : public TupleElement<Is, Types>... {
  TupleBase()
      : TupleElement<Is, Types>()... {}

  template <typename... UTypes>
  explicit TupleBase(UTypes&&... args)
      : TupleElement<Is, Types>(std::forward<UTypes>(args))... {}
};

template <typename... Types>
class Tuple : public TupleBase<std::index_sequence_for<Types...>, Types...> {
public:
  Tuple()
    requires (std::is_default_constructible_v<Types> && ...)
      : TupleBase<std::index_sequence_for<Types...>, Types...>() {}

  template <typename... UTypes, typename = std::enable_if_t<(std::is_convertible_v<UTypes&&, Types> && ...)>>
  Tuple(UTypes&&... args)
      : TupleBase<std::index_sequence_for<Types...>, Types...>(std::forward<UTypes>(args)...) {}
};

// deduction guide to aid CTAD
template <typename... Types>
Tuple(Types...) -> Tuple<Types...>;

template <class... Types>
constexpr Tuple<std::unwrap_ref_decay_t<Types>...> make_tuple(Types&&... args) {
  return Tuple<std::unwrap_ref_decay_t<Types>...>(std::forward<Types>(args)...);
}

template <typename T>
inline constexpr std::size_t tuple_size = 0;

template <typename... Types>
inline constexpr std::size_t tuple_size<Tuple<Types...>> = sizeof...(Types);

template <std::size_t I, typename... T>
struct tuple_element_impl;

template <std::size_t I, typename Head, typename... Tail>
struct tuple_element_impl<I, Head, Tail...> : tuple_element_impl<I - 1, Tail...> {};

template <typename Head, typename... Tail>
struct tuple_element_impl<0, Head, Tail...> {
  using type = Head;
};

template <std::size_t I, typename T>
struct tuple_element_help;

template <std::size_t I, typename... Types>
struct tuple_element_help<I, Tuple<Types...>> {
  using type = typename tuple_element_impl<I, Types...>::type;
};

template <std::size_t N, typename T>
using tuple_element = tuple_element_help<N, T>::type;

template <typename Ref, typename Tuple>
auto&& get_value(Tuple&& t) noexcept {
  return static_cast<Ref>(t).value;
}

template <std::size_t I, typename... Types>
auto& get(Tuple<Types...>& t) noexcept {
  using Base = TupleElement<I, tuple_element<I, Tuple<Types...>>>;
  return get_value<Base&>(t);
}

template <std::size_t I, typename... Types>
const auto& get(const Tuple<Types...>& t) noexcept {
  using Base = TupleElement<I, tuple_element<I, Tuple<Types...>>>;
  return get_value<const Base&>(t);
}

template <std::size_t I, typename... Types>
auto&& get(Tuple<Types...>&& t) noexcept {
  using Base = TupleElement<I, tuple_element<I, Tuple<Types...>>>;
  return std::move(get_value<Base&&>(t));
}

template <std::size_t I, typename... Types>
const auto&& get(const Tuple<Types...>&& t) noexcept {
  using Base = TupleElement<I, tuple_element<I, Tuple<Types...>>>;
  return std::move(get_value<const Base&&>(t));
}

template <typename T, std::size_t I>
T& get_value_type(TupleElement<I, T>& t) noexcept {
  return t.value;
}

template <typename T, std::size_t I>
const T& get_value_type(const TupleElement<I, T>& t) noexcept {
  return t.value;
}

template <typename T, typename... Types>
T& get(Tuple<Types...>& t) {
  return get_value_type<T>(t);
}

template <typename T, typename... Types>
const T& get(const Tuple<Types...>& t) {
  return get_value_type<T>(t);
}

template <typename T, typename... Types>
T&& get(Tuple<Types...>&& t) {
  return std::move(get<T>(t));
}

template <typename T, typename... Types>
const T&& get(const Tuple<Types...>&& t) {
  return std::move(get<T>(t));
}

template <size_t... Is, typename Tuple1, typename Tuple2>
bool tuple_equal_impl(const Tuple1& lhs, const Tuple2& rhs, std::index_sequence<Is...>) {
  return ((get<Is>(lhs) == (get<Is>(rhs))) && ...);
}

template <typename... TTypes, typename... UTypes>
auto operator==(const Tuple<TTypes...>& lhs, const Tuple<UTypes...>& rhs) {
  static_assert(sizeof...(TTypes) == sizeof...(UTypes));
  return tuple_equal_impl(lhs, rhs, std::index_sequence_for<TTypes...>{});
}

template <size_t I, typename... TTypes, typename... UTypes>
auto compare_tuples_rec(const Tuple<TTypes...>& lhs, const Tuple<UTypes...>& rhs) {
  if constexpr (I + 1 == sizeof...(TTypes)) {
    return get<I>(lhs) <=> get<I>(rhs);
  } else {
    auto current_result = get<I>(lhs) <=> get<I>(rhs);

    if (!std::is_eq(current_result)) {
      return current_result;
    } else {
      return compare_tuples_rec<I + 1>(lhs, rhs);
    }
  }
}

template <typename... TTypes, typename... UTypes>
auto operator<=>(const Tuple<TTypes...>& lhs, const Tuple<UTypes...>& rhs) {
  static_assert(sizeof...(TTypes) == sizeof...(UTypes));
  return compare_tuples_rec<0>(lhs, rhs);
}

template <typename Res, typename... Args>
Res tuple_cat_impl(Args&&... args);

template <typename Res, typename Tuple1, typename... Tuples, size_t... Is, typename... Args>
Res tuple_cat_impl_helper(std::index_sequence<Is...>, Tuple1&& t1, Tuples&&... rest, Args&&... args) {
  return tuple_cat_impl<Res>(
      std::forward<Tuples>(rest)...,
      std::forward<Args>(args)...,
      get<Is>(std::forward<Tuple1>(t1))...
  );
}

template <typename Res, typename Tuple1, typename... Tuples, typename... Args>
Res tuple_cat_impl(Tuple1&& t1, Tuples&&... rest, Args&&... args) {
  return tuple_cat_impl_helper<Res>(
      std::make_index_sequence<tuple_size<std::decay_t<Tuple1>>>{},
      std::forward<Tuple1>(t1),
      std::forward<Tuples>(rest)...,
      std::forward<Args>(args)...
  );
}

template <typename Res, typename... Args>
Res tuple_cat_impl(Args&&... args) {
  return Res(std::forward<Args>(args)...);
}

template <typename T1, typename T2>
struct concat2;

template <typename... Types1, typename... Types2>
struct concat2<Tuple<Types1...>, Tuple<Types2...>> {
  using type = Tuple<Types1..., Types2...>;
};

template <typename... Types>
struct concated_type;

template <>
struct concated_type<> {
  using type = Tuple<>;
};

template <>
struct concated_type<Tuple<>> {
  using type = Tuple<>;
};

template <typename... Types>
struct concated_type<Tuple<Types...>> {
  using type = Tuple<Types...>;
};

template <typename... Types, typename... Rest>
struct concated_type<Tuple<Types...>, Rest...> {
  using type = concat2<Tuple<Types...>, typename concated_type<Rest...>::type>::type;
};

template <typename... Tuples>
auto tuple_cat(Tuples&&... args) {
  return tuple_cat_impl<typename concated_type<std::remove_cvref_t<Tuples>...>::type>(std::forward<Tuples>(args)...);
}

} // namespace ct
