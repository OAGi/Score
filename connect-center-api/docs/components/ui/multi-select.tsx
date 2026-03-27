'use client';

import { Check, ChevronDown, X } from 'lucide-react';
import * as React from 'react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';

type Option = {
  label: string;
  value: string;
};

export function MultiSelect({
  options,
  values,
  onValuesChange,
  placeholder = 'Select options',
  maxSelected,
  className,
}: {
  options: Option[];
  values: string[];
  onValuesChange: (values: string[]) => void;
  placeholder?: string;
  maxSelected?: number;
  className?: string;
}) {
  const [open, setOpen] = React.useState(false);

  const toggle = (value: string) => {
    if (values.includes(value)) {
      onValuesChange(values.filter((v) => v !== value));
    } else {
      if (maxSelected === 1) {
        onValuesChange([value]);
        setOpen(false);
        return;
      }
      onValuesChange([...values, value]);
    }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          className={cn('h-auto min-h-9 w-full justify-between px-3 py-1.5 text-left text-sm font-normal', className)}
        >
          <div className="flex min-h-5 flex-1 flex-wrap gap-1">
            {values.length > 0 ? (
              values.map((value) => (
                <Badge
                  key={value}
                  className="gap-1 border-border bg-[#f8fafc] text-[#334155]"
                  onClick={(event) => {
                    event.stopPropagation();
                    toggle(value);
                  }}
                >
                  {value}
                  <X className="h-3 w-3" />
                </Badge>
              ))
            ) : (
              <span className="text-[#94a3b8]">{placeholder}</span>
            )}
          </div>
          <ChevronDown className="h-4 w-4 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[280px] p-0">
        <Command>
          <CommandInput placeholder="Search..." />
          <CommandList>
            <CommandEmpty>No results found.</CommandEmpty>
            <CommandGroup>
              {options.map((option) => {
                const selected = values.includes(option.value);
                return (
                  <CommandItem key={option.value} onSelect={() => toggle(option.value)}>
                    <span
                      className={cn(
                        'mr-2 flex h-4 w-4 items-center justify-center rounded-sm border',
                        selected ? 'border-[#0f172a] bg-[#0f172a] text-white' : 'border-border',
                      )}
                    >
                      {selected ? <Check className="h-3 w-3" /> : null}
                    </span>
                    {option.label}
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
