data class OptionT<F, A>(val value: Kind<F, Option<A>>) {
	object K

	companion object {
		fun <F, A> pure(AP: Applicative<F>): (A) -> OptionT<F, A> = { v ->
			OptionT(AP.pure(Some(v)))
		}

		fun <F, A> none(AP: Applicative<F>): OptionT<F, A> {
			return OptionT(AP.pure(None))
		}

		fun <F, A> liftF(M: Functor<F>): (Kind<F, A>) -> OptionT<F, A> = { fa ->
			val v = M.run {
				fa.map {
					Some(it)
				}
			}
			OptionT(v)
		}
	}

	fun <B> flatMap(M: Monad<F>): ((A) -> OptionT<F, B>) -> OptionT<F, B> = { f ->
		val r = M.run {
			value.flatMap { oa ->
				when (oa) {
					is Some -> f(oa.value).value
					else -> M.pure(None)
				}
			}
		}
		OptionT(r)
	}

	fun <B> map(F: Functor<F>): ((A) -> B) -> OptionT<F, B> = { f ->
		val r: Kind<F, Option<B>> = F.run {
			value.map { ov ->
				OptionMonad.run {
					ov.map(f).unwrap()
				}
			}
		}
		OptionT(r)
	}

}


fun main(args: Array<String>) {
	val fa1 = OptionT.pure<List.K, Int>(ListMonad)(1)
	val fa2 = OptionT.pure<List.K, Int>(ListMonad)(2)
	val fa3 = fa1.flatMap<Int>(ListMonad)({ a1: Int ->
		fa2.map<Int>(ListMonad)({ a2: Int ->
			a1 + a2
		})
	})
	println(fa3.value)
}
