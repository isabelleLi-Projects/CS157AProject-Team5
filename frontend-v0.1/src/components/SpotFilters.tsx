type SpotFiltersProps = {
  filters: string[]
  activeFilter: string
  onFilterChange: (filter: string) => void
}

export default function SpotFilters({ filters, activeFilter, onFilterChange }: SpotFiltersProps) {
  return (
    <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
      {filters.map((filter, index) => (
        <button
          key={filter}
          onClick={() => onFilterChange(filter)}
          className={`shrink-0 text-xs font-medium px-3 py-1.5 rounded-full standard-border transition-colors ${
            filter === activeFilter
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
