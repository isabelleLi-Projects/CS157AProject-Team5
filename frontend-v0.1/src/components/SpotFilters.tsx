type SpotFiltersProps = {
  filters: string[]
}

export default function SpotFilters({ filters }: SpotFiltersProps) {
  return (
    <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
      {filters.map((filter, index) => (
        <button
          key={filter}
          className={`shrink-0 text-xs font-medium px-3 py-1.5 rounded-full standard-border transition-colors ${
            index === 0
              ? 'primary-background'
              : 'surface-background text-secondary hover:border-[#2d5a3d] hover:text-primary'
          }`}
        >
          {filter}
        </button>
      ))}
    </div>
  )
}
