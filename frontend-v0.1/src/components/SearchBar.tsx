type SearchBarProps = {
  search: string
  onSearchChange: (value: string) => void
  onSearch: () => void
}

export default function SearchBar({ search, onSearchChange, onSearch }: SearchBarProps) {
  return (
    <div className="relative mb-6">
      <span className="absolute left-4 top-1/2 -translate-y-1/2 text-muted">🔍</span>
      <input
        type="text"
        value={search}
        onChange={(event) => onSearchChange(event.target.value)}
        placeholder="Search by name, building, or tag…"
        className="search-input pl-11 pr-24 py-3.5"
      />
      <button onClick={onSearch} className="btn-primary absolute right-3 top-1/2 -translate-y-1/2 text-xs px-3 py-1.5">
        Search
      </button>
    </div>
  )
}
