type HeroSectionProps = {
  isLoggedIn: boolean
}

export default function HeroSection({ isLoggedIn }: HeroSectionProps) {
  return (
    <section className="pt-8 pb-6">
      {isLoggedIn ? (
        <>
          <p className="text-sm text-muted mb-1">Good afternoon, Isabelle</p>
          <h1 className="font-display text-3xl text-main leading-tight">
            Find your perfect <span className="text-primary">study spot</span>
          </h1>
        </>
      ) : (
        <>
          <h1 className="font-display text-3xl text-main mb-3">
            Discover quiet places <span>to </span>
            <span className="text-primary">study &amp; focus.</span>
          </h1>
          <div className="flex gap-2">
            <button className="btn-primary px-4 py-2">Log In</button>
            <button className="btn-secondary px-4 py-2">Continue as Guest</button>
          </div>
        </>
      )}
    </section>
  )
}
